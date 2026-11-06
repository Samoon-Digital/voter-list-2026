package com.samoondigital.yojnaplus.feature.splash

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.samoondigital.yojnaplus.R

/** Image-free startup UI. Its lifetime follows ad readiness and the bounded startup window. */
@Composable
fun StartupSplashScreen(versionName: String, modifier: Modifier = Modifier) {
    SplashSystemBarEffect()
    BoxWithConstraints(
        modifier.fillMaxSize().background(Color.White)
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        val compact = maxHeight < 420.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.app_name),
                    color = Color(0xFF241B59),
                    fontSize = if (compact) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                if (!compact) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.ui_wizard_title),
                        color = Color(0xFF64748B), fontSize = 14.sp, textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(if (compact) 20.dp else 32.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(if (compact) 28.dp else 36.dp),
                    color = Color(0xFF3D2EAE), strokeWidth = 3.dp,
                    trackColor = Color(0xFFE9E6F6),
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.ui_splash_loading), color = Color(0xFF475569), fontSize = 14.sp)
            }
            Text(
                stringResource(R.string.ui_splash_ad_notice),
                color = Color(0xFF64748B), fontSize = 12.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (compact) 12.dp else 24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.ui_splash_footer), color = Color(0xFF64748B), fontSize = 11.sp)
                Text("·", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Text(stringResource(R.string.ui_splash_version, versionName), color = Color(0xFF64748B), fontSize = 11.sp)
            }
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
