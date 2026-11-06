package com.samoondigital.yojnaplus.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class StartupAdPhase { Preparing, Showing, Finished }

/** Main-thread state: wait only within the original startup window, never over app content. */
internal class StartupAdGate(private val maxWaitMs: Long = 6_000L) {
    private val _phase = MutableStateFlow(StartupAdPhase.Preparing)
    val phase = _phase.asStateFlow()
    private var startedAtMs: Long? = null
    private var loadStarted = false

    fun startLoading(): Boolean {
        if (loadStarted || _phase.value != StartupAdPhase.Preparing) return false
        loadStarted = true
        return true
    }

    fun startWindow(nowMs: Long): Boolean {
        if (!loadStarted || startedAtMs != null || _phase.value != StartupAdPhase.Preparing) return false
        startedAtMs = nowMs
        return true
    }

    fun remainingWaitMs(nowMs: Long): Long =
        startedAtMs?.let { (maxWaitMs - (nowMs - it)).coerceIn(0L, maxWaitMs) } ?: 0L

    fun acceptsLoadedAd(nowMs: Long): Boolean =
        loadStarted && _phase.value == StartupAdPhase.Preparing &&
            (startedAtMs == null || remainingWaitMs(nowMs) > 0L)

    fun tryShow(adAvailable: Boolean, eligible: Boolean, nowMs: Long): Boolean {
        if (_phase.value != StartupAdPhase.Preparing) return false
        if (expire(nowMs) || !adAvailable || !eligible || startedAtMs == null) return false
        _phase.value = StartupAdPhase.Showing
        return true
    }

    fun expire(nowMs: Long): Boolean {
        if (_phase.value != StartupAdPhase.Preparing || startedAtMs == null || remainingWaitMs(nowMs) > 0L) {
            return false
        }
        finish()
        return true
    }

    fun finish() {
        _phase.value = StartupAdPhase.Finished
    }
}
