package com.samoondigital.yojnaplus.pdf

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun downloadCdnPdf(
        cdnPath: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf = withContext(Dispatchers.IO) {
        val url = if (cdnPath.startsWith("http")) {
            cdnPath
        } else {
            "https://voters.eci.gov.in/eroll/$cdnPath"
        }
        val fileName = sanitizeFileName(url.substringAfterLast('/').ifBlank { "electoral-roll.pdf" })
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading voter list PDF")
            .setMimeType("application/pdf")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "VoterList2026/$fileName",
            )
        val id = downloadManager.enqueue(request)
        pollDownload(id, fileName, onProgress)
    }

    suspend fun saveBase64Pdf(
        base64Pdf: String,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf = withContext(Dispatchers.IO) {
        val bytes = Base64.decode(base64Pdf, Base64.DEFAULT)
        val safeName = sanitizeFileName(fileName.ifBlank { "electoral-roll.pdf" })
            .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/VoterList2026")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = context.contentResolver.insert(collection, values)
                ?: throw IllegalStateException("Unable to create PDF file")
            context.contentResolver.openOutputStream(itemUri)?.use { output ->
                output.write(bytes)
            } ?: throw IllegalStateException("Unable to write PDF file")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(itemUri, values, null, null)
            itemUri
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "VoterList2026")
            dir.mkdirs()
            val file = File(dir, safeName)
            file.writeBytes(bytes)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        onProgress(100)
        DownloadedPdf(fileName = safeName, uri = uri.toString())
    }

    fun openPdf(downloadedPdf: DownloadedPdf) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(downloadedPdf.uri), "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private suspend fun pollDownload(
        id: Long,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf {
        while (true) {
            val query = DownloadManager.Query().setFilterById(id)
            downloadManager.query(query).use { cursor ->
                if (cursor.moveToFirst()) {
                    val status = cursor.intValue(DownloadManager.COLUMN_STATUS)
                    val downloaded = cursor.longValue(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val total = cursor.longValue(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    if (total > 0L) {
                        onProgress(((downloaded * 100) / total).toInt().coerceIn(0, 100))
                    }
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            onProgress(100)
                            val uri = downloadManager.getUriForDownloadedFile(id)
                                ?: throw IllegalStateException("Downloaded file URI not found")
                            return DownloadedPdf(fileName = fileName, uri = uri.toString())
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.intValue(DownloadManager.COLUMN_REASON)
                            throw IllegalStateException("Download failed ($reason)")
                        }
                    }
                }
            }
            delay(500)
        }
    }

    private fun android.database.Cursor.intValue(column: String): Int =
        getInt(getColumnIndexOrThrow(column))

    private fun android.database.Cursor.longValue(column: String): Long =
        getLong(getColumnIndexOrThrow(column))

    private fun sanitizeFileName(value: String): String =
        value.replace(Regex("[\\\\/:*?\"<>|]"), "_")
}

data class DownloadedPdf(
    val fileName: String,
    val uri: String,
)
