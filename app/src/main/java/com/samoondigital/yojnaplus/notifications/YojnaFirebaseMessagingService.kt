package com.samoondigital.yojnaplus.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samoondigital.yojnaplus.R

class YojnaFirebaseMessagingService : FirebaseMessagingService() {
    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        FirebaseIntegrationManager.handleRegistration(installationId, source = "registered")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.notification
        val title = notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: "Tap to view the latest update."

        Log.d(
            "FirebaseMessaging",
            "message-received from=${message.from} title=$title dataKeys=${message.data.keys}",
        )
        AppNotificationManager.showRemoteNotification(
            context = this,
            title = title,
            body = body,
            data = message.data,
        )
    }
}
