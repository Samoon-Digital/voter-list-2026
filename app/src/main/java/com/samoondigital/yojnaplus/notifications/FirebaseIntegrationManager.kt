package com.samoondigital.yojnaplus.notifications

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseIntegrationManager {
    private const val Tag = "FirebaseIntegration"

    @Volatile
    private var initialized = false

    fun initialize(application: Application) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initialized = true
        }

        FirebaseAnalytics.getInstance(application)
        AppNotificationManager.createNotificationChannel(application)
        FirebaseMessaging.getInstance().register().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(Tag, "fcm-registration source=initial status=failure", task.exception)
                return@addOnCompleteListener
            }
            Log.d(Tag, "fcm-registration source=initial status=requested")
        }
        Log.d(Tag, "initialized package=${application.packageName}")
    }

    fun handleRegistration(installationId: String?, source: String) {
        if (installationId.isNullOrBlank()) {
            Log.w(Tag, "fcm-registration source=$source status=empty")
            return
        }
        Log.d(Tag, "fcm-registration source=$source status=received length=${installationId.length}")
        onRegistrationReadyForBackend(installationId, source)
    }

    private fun onRegistrationReadyForBackend(installationId: String, source: String) {
        // Future backend integration point: send this FCM registration identifier to your API when backend is ready.
        Log.d(Tag, "fcm-registration backend-sync-ready source=$source length=${installationId.length}")
    }
}
