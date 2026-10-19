package com.samoondigital.yojnaplus.ads

/** Accessed on the main thread; callers supply monotonic elapsed time. */
internal class InterstitialFrequencyGate(private val cooldownMillis: Long = 120_000L) {
    private var showing = false
    private var lastFinishedAt: Long? = null

    fun tryStart(now: Long): Boolean {
        if (showing || lastFinishedAt?.let { now - it < cooldownMillis } == true) return false
        showing = true
        return true
    }

    fun finish(now: Long) {
        showing = false
        lastFinishedAt = now
    }
}
