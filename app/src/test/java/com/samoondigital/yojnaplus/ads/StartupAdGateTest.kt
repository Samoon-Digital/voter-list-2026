package com.samoondigital.yojnaplus.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupAdGateTest {
    @Test fun readyAdShowsImmediatelyWithinTheWindowAndCannotShowTwice() {
        val gate = StartupAdGate()
        assertTrue(gate.startLoading())
        assertTrue(gate.startWindow(1_000))
        assertTrue(gate.tryShow(adAvailable = true, eligible = true, nowMs = 1_500))
        assertEquals(StartupAdPhase.Showing, gate.phase.value)
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 1_501))
        gate.finish()
        assertFalse(gate.startLoading())
        assertFalse(gate.acceptsLoadedAd(2_000))
    }

    @Test fun themeReadinessDoesNotCancelAnAdThatIsStillLoading() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertFalse(gate.tryShow(adAvailable = false, eligible = true, nowMs = 1_150))
        assertEquals(StartupAdPhase.Preparing, gate.phase.value)
        assertTrue(gate.acceptsLoadedAd(3_000))
        assertTrue(gate.tryShow(adAvailable = true, eligible = true, nowMs = 3_000))
    }

    @Test fun readyAdWaitsForConsentAndAResumedLoadingScreen() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertFalse(gate.tryShow(adAvailable = true, eligible = false, nowMs = 2_000))
        assertEquals(StartupAdPhase.Preparing, gate.phase.value)
        assertTrue(gate.tryShow(adAvailable = true, eligible = true, nowMs = 4_000))
    }

    @Test fun missingAdTimesOutAtSixSecondsAndRejectsAllLateCallbacks() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertFalse(gate.expire(6_999))
        assertEquals(1L, gate.remainingWaitMs(6_999))
        assertTrue(gate.expire(7_000))
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
        assertFalse(gate.acceptsLoadedAd(7_001))
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 7_001))
    }

    @Test fun lateLoadCannotShowEvenIfTheTimeoutHandlerHasNotRunYet() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 7_000))
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
    }

    @Test fun lastMillisecondIsEligibleButTheDeadlineDoesNotDismissAShowingAd() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertTrue(gate.tryShow(adAvailable = true, eligible = true, nowMs = 6_999))
        assertFalse(gate.expire(20_000))
        assertEquals(StartupAdPhase.Showing, gate.phase.value)
        gate.finish()
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
    }

    @Test fun recreationOrRepeatedInitializationCannotExtendTheOriginalDeadline() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        assertFalse(gate.startLoading())
        assertFalse(gate.startWindow(5_000))
        assertEquals(2_000L, gate.remainingWaitMs(5_000))
        assertTrue(gate.expire(7_000))
    }

    @Test fun backgroundOrLoadFailureClosesTheWindowWithoutWaitingForTheDeadline() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.startWindow(1_000)
        gate.finish()
        assertFalse(gate.acceptsLoadedAd(2_000))
        assertFalse(gate.startLoading())
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 2_000))
    }

    @Test fun preloadingBeforeTheFirstFrameDoesNotUseUpTheVisibleLoadingWindow() {
        val gate = StartupAdGate()
        gate.startLoading()
        assertTrue(gate.acceptsLoadedAd(8_000))
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 8_000))
        assertTrue(gate.startWindow(8_000))
        assertEquals(6_000L, gate.remainingWaitMs(8_000))
        assertFalse(gate.expire(13_999))
        assertTrue(gate.expire(14_000))
    }

    @Test fun cachedAdCanShowAsSoonAsTheLoadingFrameAndConsentAreReady() {
        val gate = StartupAdGate()
        gate.startLoading()
        assertTrue(gate.acceptsLoadedAd(500))
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 500))
        gate.startWindow(1_000)
        assertTrue(gate.tryShow(adAvailable = true, eligible = true, nowMs = 1_001))
    }

    @Test fun noRequestCannotShowAnAd() {
        val gate = StartupAdGate()
        assertFalse(gate.tryShow(adAvailable = true, eligible = true, nowMs = 1_000))
    }
}
