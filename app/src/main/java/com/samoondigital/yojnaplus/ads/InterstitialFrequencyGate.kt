package com.samoondigital.yojnaplus.ads

/** Main-thread guard against stacked ads. There is no time-based cooldown. */
internal class InterstitialFrequencyGate {
    private var showing = false

    fun tryStart(): Boolean {
        if (showing) return false
        showing = true
        return true
    }

    fun finish() {
        showing = false
    }
}
