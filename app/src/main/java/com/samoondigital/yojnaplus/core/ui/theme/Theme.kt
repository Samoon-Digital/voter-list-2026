package com.samoondigital.yojnaplus.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = OnIndigoContainer,
    secondary = Green,
    onSecondary = Color.White,
    secondaryContainer = GreenContainer,
    onSecondaryContainer = OnGreenContainer,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7B0F0),
    onPrimary = IndigoDark,
    primaryContainer = IndigoDark,
    onPrimaryContainer = IndigoContainer,
    secondary = Color(0xFF8FD7A4),
    onSecondary = GreenDark,
    secondaryContainer = GreenDark,
    onSecondaryContainer = GreenContainer,
    background = SurfaceDark,
    onBackground = Color(0xFFE4E2EA),
    surface = Color(0xFF1B1C22),
    onSurface = Color(0xFFE4E2EA),
)

@Composable
fun VoterList2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
