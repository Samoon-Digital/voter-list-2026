package com.samoondigital.yojnaplus.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialFrequencyGateTest {
    @Test fun firstAdIsAllowedAndConcurrentShowsAreBlocked() {
        val gate = InterstitialFrequencyGate()
        assertTrue(gate.tryStart(0))
        assertFalse(gate.tryStart(1))
        assertFalse(gate.tryStart(300_000))
    }

    @Test fun twoMinuteCooldownStartsAfterTheAdFinishes() {
        val gate = InterstitialFrequencyGate()
        assertTrue(gate.tryStart(1_000))
        gate.finish(301_000)
        assertFalse(gate.tryStart(420_999))
        assertTrue(gate.tryStart(421_000))
    }

    @Test fun anotherJourneyCannotBypassCooldown() {
        val gate = InterstitialFrequencyGate()
        assertTrue(gate.tryStart(100))
        gate.finish(200)
        repeat(3) { assertFalse(gate.tryStart(60_200)) }
        assertTrue(gate.tryStart(120_200))
    }
}
