package com.samoondigital.yojnaplus.core.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.max

@Composable
fun Modifier.stableStatusBarsPadding(): Modifier {
    val configuration = LocalConfiguration.current
    val currentTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().value
    var stableTop by rememberSaveable(
        configuration.orientation,
        configuration.screenWidthDp,
        configuration.screenHeightDp,
    ) { mutableStateOf(currentTop) }
    val appliedTop = max(stableTop, currentTop)

    SideEffect {
        if (currentTop > stableTop) stableTop = currentTop
    }

    return padding(top = appliedTop.dp)
}

@Composable
fun PurpleHeaderStatusBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window == null || view.isInEditMode) {
            onDispose { }
        } else {
            @Suppress("DEPRECATION")
            val previousColor = window.statusBarColor
            val controller = WindowInsetsControllerCompat(window, view)
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            @Suppress("DEPRECATION")
            window.statusBarColor = Color(0xFF240E65).toArgb()
            controller.isAppearanceLightStatusBars = false

            onDispose {
                @Suppress("DEPRECATION")
                window.statusBarColor = previousColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }
}
