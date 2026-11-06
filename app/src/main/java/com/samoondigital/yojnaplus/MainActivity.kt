package com.samoondigital.yojnaplus

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.ads.ConsentManager
import com.samoondigital.yojnaplus.ads.StartupAdPhase
import com.samoondigital.yojnaplus.core.navigation.AppNavHost
import com.samoondigital.yojnaplus.core.ui.components.AppHeaderSystemBarEffect
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.feature.splash.StartupSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private var immediateUpdateFlowStarted = false
    private var firstFrameWorkStarted by mutableStateOf(false)
    private var consentFlowComplete = false
    private var deferredStartupStarted = false

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
        restoreDefaultSystemBarLayout()
        if (savedInstanceState != null) AppOpenAdManager.skipStartup("activity-restored")
        setContent {
            if (!firstFrameWorkStarted) {
                StartupSplashScreen()
                return@setContent
            }
            val uiState by viewModel.state.collectAsStateWithLifecycle()
            val startupPhase by AppOpenAdManager.startupPhase.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.isThemeReady) {
                if (uiState.isThemeReady) AppOpenAdManager.onAppReady(this@MainActivity, consentFlowComplete)
            }
            if (!uiState.isThemeReady || startupPhase != StartupAdPhase.Finished) {
                StartupSplashScreen()
                return@setContent
            }

            val systemDark = isSystemInDarkTheme()
            VoterList2026Theme(darkTheme = uiState.darkMode ?: systemDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
                AppHeaderSystemBarEffect()
            }
            LaunchedEffect(Unit) {
                withFrameNanos { }
                window.decorView.post {
                    if (!isFinishing && !isDestroyed) initializeDeferredStartup()
                }
            }
        }
        // Post from pre-draw so the lightweight frame is drawn before SDK/consent work starts.
        window.decorView.doOnPreDraw {
            window.decorView.post {
                if (isFinishing || isDestroyed) return@post
                Log.d(Tag, "first-frame-drawn action=prioritize-startup-ad")
                AppOpenAdManager.preload(application)
                ConsentManager.gatherConsent(this, onComplete = { consentFlowComplete = true }) {
                    AdManager.allowAdRequests()
                }
                firstFrameWorkStarted = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restoreDefaultSystemBarLayout()
        if (deferredStartupStarted) resumeImmediateUpdateIfNeeded()
    }

    private fun initializeDeferredStartup() {
        if (deferredStartupStarted) return
        deferredStartupStarted = true
        Log.d(Tag, "content-ready action=initialize-deferred-startup")
        (application as YojnaPlusApp).initializeDeferredStartup()
        checkForImmediateUpdate()
    }

    private fun restoreDefaultSystemBarLayout() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.requestApplyInsets(window.decorView)
        window.decorView.post { ViewCompat.requestApplyInsets(window.decorView) }
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

    private companion object {
        const val Tag = "InAppUpdate"
    }
}
