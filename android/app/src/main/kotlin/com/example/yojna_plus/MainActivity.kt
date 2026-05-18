package com.samoondigital.yojnaplus

import android.app.DownloadManager
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.content.res.Configuration
import android.provider.MediaStore
import android.content.ContentValues
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : FlutterActivity() {
    private val downloadChannelName = "com.samoondigital.yojnaplus/downloads"
    private var downloadChannel: MethodChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge without using deprecated setters
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = !isNight
        controller.isAppearanceLightNavigationBars = !isNight
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        downloadChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, downloadChannelName).apply {
            setMethodCallHandler(::handleDownloadMethod)
        }
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        downloadChannel?.setMethodCallHandler(null)
        downloadChannel = null
        super.cleanUpFlutterEngine(flutterEngine)
    }

    private fun handleDownloadMethod(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "enqueueDownload" -> handleEnqueueDownload(call, result)
            "needsStoragePermission" -> {
                val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                result.success(needsPermission)
            }
            "openDownloadsUI" -> {
                try {
                    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("open_downloads_error", e.localizedMessage, null)
                }
            }
            "getDownloadedFileUri" -> {
                val idNumber = call.argument<Number>("id")
                if (idNumber == null) {
                    result.error("invalid_arguments", "id missing", null)
                    return
                }
                val dm = getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
                if (dm == null) {
                    result.error("service_unavailable", "DownloadManager unavailable", null)
                    return
                }
                val uri = dm.getUriForDownloadedFile(idNumber.toLong())
                result.success(uri?.toString())
            }
            "saveToDownloads" -> {
                val fileName = call.argument<String>("fileName")?.takeIf { it.isNotBlank() }
                val mimeType = call.argument<String>("mimeType")?.takeIf { it.isNotBlank() }
                val base64 = call.argument<String>("bytesBase64")
                if (fileName == null || base64.isNullOrBlank()) {
                    result.error("invalid_arguments", "fileName or bytes missing", null)
                    return
                }
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    var uriString: String? = null
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            mimeType?.let { put(MediaStore.MediaColumns.MIME_TYPE, it) }
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        if (uri == null) {
                            result.error("save_failed", "MediaStore insert failed", null)
                            return
                        }
                        contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(bytes)
                            os.flush()
                        }
                        uriString = uri.toString()
                    } else {
                        // Pre-Android Q fallback: write to public Downloads directory
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!dir.exists()) dir.mkdirs()
                        val target = java.io.File(dir, fileName)
                        target.outputStream().use { it.write(bytes) }
                        uriString = target.absolutePath
                    }
                    
                    // Show Notification
                    showDownloadNotification(fileName, mimeType)
                    
                    result.success(uriString)
                    
                } catch (e: Exception) {
                    result.error("save_failed", e.localizedMessage, null)
                }
            }
            "readContentUriBytes" -> {
                val uriStr = call.argument<String>("uri")
                if (uriStr.isNullOrBlank()) {
                    result.error("invalid_arguments", "uri missing", null)
                    return
                }
                try {
                    val uri = Uri.parse(uriStr)
                    contentResolver.openInputStream(uri)?.use { input ->
                        val buffer = ByteArrayOutputStream()
                        val data = ByteArray(8192)
                        while (true) {
                            val n = input.read(data)
                            if (n <= 0) break
                            buffer.write(data, 0, n)
                        }
                        val b64 = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP)
                        result.success(b64)
                    } ?: run {
                        result.error("open_failed", "unable to open uri", null)
                    }
                } catch (e: Exception) {
                    result.error("read_failed", e.localizedMessage, null)
                }
            }
            "getContentInfo" -> {
                val uriStr = call.argument<String>("uri")
                if (uriStr.isNullOrBlank()) {
                    result.error("invalid_arguments", "uri missing", null)
                    return
                }
                try {
                    val uri = Uri.parse(uriStr)
                    val projection = arrayOf(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.SIZE
                    )
                    contentResolver.query(uri, projection, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val name = c.getString(0)
                            val mime = c.getString(1)
                            val size = c.getLong(2)
                            val map = hashMapOf<String, Any?>(
                                "displayName" to name,
                                "mimeType" to mime,
                                "size" to size
                            )
                            result.success(map)
                            return@use
                        }
                    }
                    result.success(null)
                } catch (e: Exception) {
                    result.error("query_failed", e.localizedMessage, null)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun showDownloadNotification(fileName: String, mimeType: String?) {
        val channelId = "downloads_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Downloads",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Download completions"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Use system icon or your own
            .setContentTitle("Download Complete")
            .setContentText(fileName)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun handleEnqueueDownload(call: MethodCall, result: MethodChannel.Result) {
        val arguments = call.arguments as? Map<*, *>
        if (arguments == null) {
            result.error("invalid_arguments", "Arguments missing", null)
            return
        }

        val url = (arguments["url"] as? String)?.takeIf { it.isNotBlank() }
        val fileName = (arguments["fileName"] as? String)?.takeIf { it.isNotBlank() }
        if (url == null || fileName == null) {
            result.error("invalid_arguments", "url or fileName missing", null)
            return
        }

        val mimeType = (arguments["mimeType"] as? String)?.takeIf { it.isNotBlank() }
        val description = (arguments["description"] as? String)?.takeIf { it.isNotBlank() }
        val contentDisposition = (arguments["contentDisposition"] as? String)?.takeIf { it.isNotBlank() }

        @Suppress("UNCHECKED_CAST")
        val rawHeaders = arguments["headers"] as? Map<*, *>
        val headers = rawHeaders?.mapNotNull { entry ->
            val key = entry.key?.toString()?.takeIf { it.isNotBlank() }
            val value = entry.value?.toString()?.takeIf { it.isNotBlank() }
            if (key != null && value != null) key to value else null
        }?.toMap().orEmpty()

        try {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager == null) {
                result.error("service_unavailable", "DownloadManager unavailable", null)
                return
            }

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            description?.let { request.setDescription(it) }
            mimeType?.let { request.setMimeType(it) }
            contentDisposition?.let { request.addRequestHeader("Content-Disposition", it) }
            headers.forEach { (key, value) -> request.addRequestHeader(key, value) }

            val downloadId = downloadManager.enqueue(request)
            result.success(downloadId)
        } catch (ex: SecurityException) {
            result.error("permission_denied", ex.localizedMessage, null)
        } catch (ex: Exception) {
            result.error("download_error", ex.localizedMessage, null)
        }
    }
}

