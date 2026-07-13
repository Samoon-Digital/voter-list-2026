package com.samoondigital.yojnaplus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.ConsentManager
import com.samoondigital.yojnaplus.core.navigation.AppNavHost
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.notifications.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var appUpdateManager: AppUpdateManager
    private var immediateUpdateFlowStarted = false

    private val immediateUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        immediateUpdateFlowStarted = false
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(Tag, "immediate-update-result resultCode=${result.resultCode}")
        } else {
            Log.d(Tag, "immediate-update-result status=ok")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForImmediateUpdate()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
            VoterList2026Theme(darkTheme = darkMode ?: systemDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
        handleNotificationIntent(intent)
        ConsentManager.gatherConsent(this) {
            AdManager.allowAdRequests()
        }
    }

    override fun onResume() {
        super.onResume()
        resumeImmediateUpdateIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun checkForImmediateUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            startImmediateUpdate(appUpdateInfo, source = "available")
                        } else {
                            Log.d(Tag, "immediate-update-skipped reason=not-allowed")
                        }
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        startImmediateUpdate(appUpdateInfo, source = "in-progress")
                    }
                    else -> Unit
                }
            }
            .addOnFailureListener { error ->
                Log.w(Tag, "immediate-update-check-failed", error)
            }
    }

    private fun resumeImmediateUpdateIfNeeded() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startImmediateUpdate(appUpdateInfo, source = "resume")
                }
            }
            .addOnFailureListener { error ->
                Log.w(Tag, "immediate-update-resume-check-failed", error)
            }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo, source: String) {
        if (immediateUpdateFlowStarted) return
        immediateUpdateFlowStarted = true
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                immediateUpdateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
        }.onSuccess {
            Log.d(Tag, "immediate-update-started source=$source")
        }.onFailure { error ->
            immediateUpdateFlowStarted = false
            Log.w(Tag, "immediate-update-start-failed source=$source", error)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(AppNotificationManager.ExtraFromNotification, false) == true) {
            Log.d(
                "FirebaseMessaging",
                "notification-click-opened extras=${intent.extras?.keySet().orEmpty()}",
            )
        }
    }

    private companion object {
        const val Tag = "InAppUpdate"
    }
}
