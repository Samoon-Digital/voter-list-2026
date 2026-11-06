package com.samoondigital.yojnaplus.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class StartupAdPhase { Preparing, Showing, Finished }

/** Main-thread state: an ad gets one chance before content becomes available. */
internal class StartupAdGate {
    private val _phase = MutableStateFlow(StartupAdPhase.Preparing)
    val phase = _phase.asStateFlow()
    private var loadStarted = false

    fun startLoading(): Boolean {
        if (loadStarted || _phase.value != StartupAdPhase.Preparing) return false
        loadStarted = true
        return true
    }

    fun acceptsLoadedAd(): Boolean = loadStarted && _phase.value == StartupAdPhase.Preparing

    fun onAppReady(adAvailable: Boolean, eligible: Boolean): Boolean {
        if (_phase.value != StartupAdPhase.Preparing) return false
        val show = loadStarted && adAvailable && eligible
        _phase.value = if (show) StartupAdPhase.Showing else StartupAdPhase.Finished
        return show
    }

    fun finish() {
        _phase.value = StartupAdPhase.Finished
    }
}
