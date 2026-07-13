package com.samoondigital.yojnaplus

import android.os.Bundle
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
        ConsentManager.gatherConsent(this) {
            AdManager.initialize(application)
        }
    }
}