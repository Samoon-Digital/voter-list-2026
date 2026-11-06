package com.samoondigital.yojnaplus.feature.splash

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.samoondigital.yojnaplus.R

/** The first app frame contains no image decoding, decorative drawing or timed progress. */
@Composable
fun StartupSplashScreen(modifier: Modifier = Modifier) {
    SplashSystemBarEffect()
    Box(modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF3D2EAE),
                strokeWidth = 2.dp,
            )
            Text(stringResource(R.string.ui_splash_loading), color = Color(0xFF475569), fontSize = 14.sp)
        }
    }
}

@Composable
private fun SplashSystemBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window == null || view.isInEditMode) {
            onDispose { }
        } else {
            @Suppress("DEPRECATION")
            val previousStatusColor = window.statusBarColor
            @Suppress("DEPRECATION")
            val previousNavigationColor = window.navigationBarColor
            val controller = WindowInsetsControllerCompat(window, view)
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            val previousLightNavigationBars = controller.isAppearanceLightNavigationBars

            @Suppress("DEPRECATION")
            window.statusBarColor = Color.White.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.White.toArgb()
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true

            onDispose {
                @Suppress("DEPRECATION")
                window.statusBarColor = previousStatusColor
                @Suppress("DEPRECATION")
                window.navigationBarColor = previousNavigationColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
                controller.isAppearanceLightNavigationBars = previousLightNavigationBars
            }
        }
    }
}
