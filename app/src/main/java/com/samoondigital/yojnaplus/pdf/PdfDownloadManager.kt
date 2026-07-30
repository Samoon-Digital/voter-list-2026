package com.samoondigital.yojnaplus.pdf

import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfDownloadManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun downloadCdnPdf(
        cdnPath: String,
        fileNameOverride: String? = null,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf = withContext(Dispatchers.IO) {
        val url = if (cdnPath.startsWith("http")) {
            cdnPath
        } else {
            "https://voters.eci.gov.in/eroll/$cdnPath"
        }
        val fileName = sanitizeFileName(
            fileNameOverride?.takeIf { it.isNotBlank() }
                ?: url.substringAfterLast('/').substringBefore('?').ifBlank { "electoral-roll.pdf" },
        ).let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
        val downloadUri = Uri.parse(url.replace(" ", "%20"))
        val request = DownloadManager.Request(downloadUri)
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
        if (downloadUri.host.equals(ECI_OLD_SIR_HOST, ignoreCase = true)) {
            // ECI's edge server rejects Android DownloadManager's default User-Agent.
            request.addRequestHeader("User-Agent", ECI_DOWNLOAD_USER_AGENT)
        } else if (downloadUri.host.equals(UP_ROLL_HOST, ignoreCase = true)) {
            request.addRequestHeader("User-Agent", UP_DOWNLOAD_USER_AGENT)
        } else if (downloadUri.host.equals(CHANDIGARH_ROLL_HOST, ignoreCase = true)) {
            request.addRequestHeader("User-Agent", CHANDIGARH_DOWNLOAD_USER_AGENT)
            request.addRequestHeader("Referer", "https://ceochandigarh.gov.in/pages/intensive")
        } else if (downloadUri.host.equals(DNH_ROLL_HOST, ignoreCase = true)) {
            request.addRequestHeader("User-Agent", DNH_DOWNLOAD_USER_AGENT)
            request.addRequestHeader("Referer", "https://ceodaman.nic.in/ceoindex.html")
        } else if (downloadUri.host.equals(DNH_IFRAME_HOST, ignoreCase = true)) {
            request.addRequestHeader("User-Agent", DNH_DOWNLOAD_USER_AGENT)
            request.addRequestHeader("Referer", "https://ceoddd.in/Home/PSSearch")
        } else if (downloadUri.host.equals(WB_ROLL_HOST, ignoreCase = true)) {
            request.addRequestHeader("User-Agent", WB_DOWNLOAD_USER_AGENT)
            request.addRequestHeader("Referer", "https://ceowestbengal.wb.gov.in/Roll_ps/1")
        }
        val id = downloadManager.enqueue(request)
        var completed = false
        try {
            val downloaded = pollDownload(id, fileName, onProgress)
            completed = true
            downloaded
        } finally {
            if (!completed) {
                downloadManager.remove(id)
            }
        }
    }

    suspend fun saveBase64Pdf(
        base64Pdf: String,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf = savePdfBytes(
        bytes = Base64.decode(base64Pdf, Base64.DEFAULT),
        fileName = fileName,
        onProgress = onProgress,
    )

    suspend fun savePdfBytes(
        bytes: ByteArray,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf = withContext(Dispatchers.IO) {
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

    private suspend fun pollDownload(
        id: Long,
        fileName: String,
        onProgress: (Int) -> Unit,
    ): DownloadedPdf {
        while (true) {
            kotlin.coroutines.coroutineContext.ensureActive()
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
                            val uri = publicDownloadUri(fileName)
                                ?: downloadManager.getUriForDownloadedFile(id)
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

    private fun publicDownloadUri(fileName: String): Uri? {
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
                ContentUris.withAppendedId(collection, id)
            }
        }.getOrNull()
    }
    private fun sanitizeFileName(value: String): String =
        value.replace(Regex("[\\\\/:*?\"<>|]"), "_")

    private companion object {
        const val ECI_OLD_SIR_HOST = "www.eci.gov.in"
        const val UP_ROLL_HOST = "ceouttarpradesh.nic.in"
        const val CHANDIGARH_ROLL_HOST = "ceochandigarh.gov.in"
        const val DNH_ROLL_HOST = "ceodaman.nic.in"
        const val DNH_IFRAME_HOST = "ceoddd.in"
        const val WB_ROLL_HOST = "ceowestbengal.wb.gov.in"
        const val ECI_DOWNLOAD_USER_AGENT = "curl/8.10.1 VoterList2026/Android"
        const val UP_DOWNLOAD_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36"
        const val CHANDIGARH_DOWNLOAD_USER_AGENT = UP_DOWNLOAD_USER_AGENT
        const val DNH_DOWNLOAD_USER_AGENT = UP_DOWNLOAD_USER_AGENT
        const val WB_DOWNLOAD_USER_AGENT = UP_DOWNLOAD_USER_AGENT
    }
}

data class DownloadedPdf(
    val fileName: String,
    val uri: String,
)




