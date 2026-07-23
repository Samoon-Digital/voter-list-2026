package com.samoondigital.yojnaplus.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.samoondigital.yojnaplus.MainActivity
import com.samoondigital.yojnaplus.R
import kotlin.math.absoluteValue

object AppNotificationManager {
    const val ChannelId = "voter_list_updates"
    const val ExtraFromNotification = "extra_from_notification"
    private const val Tag = "AppNotificationManager"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            ChannelId,
            "Voter List Updates",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "New voter lists, election updates, and important announcements."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        Log.d(Tag, "notification-channel-ready id=$ChannelId")
    }

    fun showRemoteNotification(
        context: Context,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        createNotificationChannel(context)
        if (!canPostNotifications(context)) {
            Log.d(Tag, "notification-skipped reason=permission-not-granted")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ExtraFromNotification, true)
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            data.hashCode().absoluteValue,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId(data), notification)
            Log.d(Tag, "notification-posted title=$title dataKeys=${data.keys}")
        } catch (securityException: SecurityException) {
            Log.w(Tag, "notification-skipped reason=permission-revoked", securityException)
        }
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationId(data: Map<String, String>): Int {
        return (data["message_id"] ?: data["google.message_id"] ?: data.hashCode().toString())
            .hashCode()
            .absoluteValue
    }
}
