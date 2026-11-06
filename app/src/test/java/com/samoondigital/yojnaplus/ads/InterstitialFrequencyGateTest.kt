package com.samoondigital.yojnaplus.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialFrequencyGateTest {
    @Test fun concurrentInterstitialsRemainBlocked() {
        val gate = InterstitialFrequencyGate()
        assertTrue(gate.tryStart())
        repeat(3) { assertFalse(gate.tryStart()) }
    }

    @Test fun anotherCompletedBatchDoesNotHaveToWaitForACooldown() {
        val gate = InterstitialFrequencyGate()
        repeat(3) {
            assertTrue(gate.tryStart())
            gate.finish()
        }
    }
}
