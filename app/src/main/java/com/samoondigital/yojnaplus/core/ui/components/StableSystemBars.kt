package com.samoondigital.yojnaplus.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
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