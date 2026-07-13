package com.samoondigital.yojnaplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.ConsentManager
import com.samoondigital.yojnaplus.core.navigation.AppNavHost
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.notifications.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(AppNotificationManager.ExtraFromNotification, false) == true) {
            Log.d(
                "FirebaseMessaging",
                "notification-click-opened extras=${intent.extras?.keySet().orEmpty()}",
            )
        }
    }
}
