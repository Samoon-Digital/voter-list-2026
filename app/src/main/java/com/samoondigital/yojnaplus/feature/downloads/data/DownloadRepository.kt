package com.samoondigital.yojnaplus.feature.downloads.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.samoondigital.yojnaplus.feature.downloads.worker.DownloadStorageSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dao: DownloadRecordDao,
) {
    private val fileMutationMutex = Mutex()

    fun observeDownloads(): Flow<List<DownloadRecordEntity>> = dao.observeAll()

    suspend fun createPendingRecord(
        district: String,
        assembly: String,
        village: String,
        partNumber: Int,
    ): String {
        val id = UUID.randomUUID().toString()
        dao.upsert(
            DownloadRecordEntity(
                id = id,
                fileName = "Part $partNumber.pdf",
                uri = null,
                district = district,
                assembly = assembly,
                village = village,
                fileSizeBytes = null,
                downloadedAtMillis = Instant.now().toEpochMilli(),
                status = DownloadStatusEntity.Waiting.name,
                progress = 0,
                errorMessage = null,
            ),
        )
        return id
    }

    suspend fun markDownloading(id: String) = updateStatus(id, DownloadStatusEntity.Downloading, progress = 0)

    suspend fun updateProgress(id: String, progress: Int) {
        val record = dao.getById(id) ?: return
        dao.update(record.copy(progress = progress.coerceIn(0, 100), status = DownloadStatusEntity.Downloading.name))
    }

    suspend fun markCompleted(id: String, fileName: String, uri: String) {
        val record = dao.getById(id) ?: return
        val storedUri = resolveReadableUri(fileName, uri) ?: uri
        dao.update(
            record.copy(
                fileName = fileName,
                uri = storedUri,
                fileSizeBytes = queryFileSize(storedUri),
                downloadedAtMillis = Instant.now().toEpochMilli(),
                status = DownloadStatusEntity.Completed.name,
                progress = 100,
                errorMessage = null,
            ),
        )
    }

    suspend fun markFailed(id: String, message: String) =
        updateStatus(id, DownloadStatusEntity.Failed, errorMessage = message)

    suspend fun markCancelled(id: String) =
        updateStatus(id, DownloadStatusEntity.Cancelled, errorMessage = null)

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        fileMutationMutex.withLock {
            val record = dao.getById(id) ?: return@withLock
            // Finish the storage change and its database update together if the screen is closed.
            withContext(NonCancellable) {
                record.uri?.let { value ->
                    val uri = value.toUri()
                    val file = localFile(uri)
                    val deleted = if (file != null) file.delete() else {
                        context.contentResolver.delete(uri, null, null) > 0
                    }
                    if (!deleted) throw IOException("Unable to delete PDF from storage")
                }
                dao.delete(record)
            }
        }
    }

    suspend fun rename(id: String, rawName: String) = withContext(Dispatchers.IO) {
        fileMutationMutex.withLock {
            val record = dao.getById(id) ?: return@withLock
            val uri = record.uri?.toUri() ?: throw IOException("PDF file is not available to rename")
            val safeName = sanitizePdfName(rawName)
            withContext(NonCancellable) {
                val source = localFile(uri)
                val updatedRecord = if (source != null) {
                    if (!source.isFile) throw IOException("PDF file is missing from storage")
                    val target = File(source.parentFile, safeName)
                    if (source != target) {
                        if (target.exists()) throw IOException("A PDF with this name already exists")
                        if (!source.renameTo(target)) throw IOException("Unable to rename PDF in storage")
                    }
                    val updatedUri = if (uri.scheme == "file") Uri.fromFile(target) else {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target)
                    }
                    record.copy(fileName = target.name, uri = updatedUri.toString())
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, safeName)
                    }
                    if (context.contentResolver.update(uri, values, null, null) <= 0) {
                        throw IOException("Unable to rename PDF in storage")
                    }
                    // MediaStore may choose a unique filename when the requested name already exists.
                    val storedName = context.contentResolver.query(
                        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    } ?: throw IOException("Unable to verify renamed PDF")
                    record.copy(fileName = storedName)
                }
                dao.update(updatedRecord)
            }
        }
    }

    private fun localFile(uri: Uri): File? {
        if (uri.scheme == "file") return File(uri.path ?: throw IOException("Invalid PDF path")).canonicalFile
        if (uri.scheme != "content" || uri.authority != "${context.packageName}.fileprovider") return null
        // Resolve only roots declared by our own FileProvider, including legacy Android downloads.
        val segments = uri.pathSegments
        val root = when (segments.firstOrNull()) {
            "external_files" -> context.getExternalFilesDir(null)
            "cache" -> context.cacheDir
            else -> null
        }?.canonicalFile ?: throw IOException("Unsupported PDF storage location")
        val file = File(root, segments.drop(1).joinToString(File.separator)).canonicalFile
        if (!file.path.startsWith(root.path + File.separator) ||
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) != uri
        ) {
            throw IOException("Invalid PDF storage location")
        }
        return file
    }

    fun share(record: DownloadRecordEntity) {
        val uri = record.uri ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri.toUri())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    suspend fun syncWithLocalStorage() = withContext(Dispatchers.IO) {
        val records = dao.getAllOnce()
        records.forEach { record ->
            val status = record.status.toStatus()
            if (status == DownloadStatusEntity.Completed && record.uri != null && !uriExists(record.uri)) {
                val repairedUri = findPublicDownloadUri(record.fileName)
                if (repairedUri != null && uriExists(repairedUri)) {
                    dao.update(
                        record.copy(
                            uri = repairedUri,
                            fileSizeBytes = queryFileSize(repairedUri),
                            status = DownloadStatusEntity.Completed.name,
                            progress = 100,
                            errorMessage = null,
                        ),
                    )
                } else {
                    dao.update(
                        record.copy(
                            status = DownloadStatusEntity.Missing.name,
                            errorMessage = "File is missing from local storage",
                        ),
                    )
                }
            }
        }
    }

    fun enqueueStorageSync() {
        val request = OneTimeWorkRequestBuilder<DownloadStorageSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadStorageSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private suspend fun updateStatus(
        id: String,
        status: DownloadStatusEntity,
        progress: Int? = null,
        errorMessage: String? = null,
    ) {
        val record = dao.getById(id) ?: return
        dao.update(
            record.copy(
                status = status.name,
                progress = progress ?: record.progress,
                errorMessage = errorMessage,
            ),
        )
    }

    private fun resolveReadableUri(fileName: String, uri: String): String? =
        uri.takeIf(::uriExists) ?: findPublicDownloadUri(fileName)

    private fun findPublicDownloadUri(fileName: String): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.Downloads._ID)
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/VoterList2026/"
        return runCatching {
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?",
                arrayOf(fileName, relativePath),
                "${MediaStore.Downloads.DATE_MODIFIED} DESC",
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                ContentUris.withAppendedId(collection, id).toString()
            }
        }.getOrNull()
    }
    private fun queryFileSize(uri: String): Long? =
        runCatching {
            context.contentResolver.query(uri.toUri(), arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else null
                    } else {
                        null
                    }
                }
        }.getOrNull()

    private fun uriExists(uri: String): Boolean =
        runCatching {
            context.contentResolver.openFileDescriptor(uri.toUri(), "r")?.use { true } == true
        }.getOrDefault(false)

    private fun sanitizePdfName(value: String): String {
        val cleaned = value.trim()
            .ifBlank { "electoral-roll.pdf" }
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return if (cleaned.endsWith(".pdf", ignoreCase = true)) cleaned else "$cleaned.pdf"
    }

    private fun String.toStatus(): DownloadStatusEntity =
        runCatching { DownloadStatusEntity.valueOf(this) }.getOrDefault(DownloadStatusEntity.Failed)
}


