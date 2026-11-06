package com.samoondigital.yojnaplus.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupAdGateTest {
    @Test fun readyAdKeepsLoadingScreenUntilDismissalAndCannotShowTwice() {
        val gate = StartupAdGate()
        assertTrue(gate.startLoading())
        assertFalse(gate.startLoading())
        assertTrue(gate.onAppReady(adAvailable = true, eligible = true))
        assertEquals(StartupAdPhase.Showing, gate.phase.value)
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
        assertEquals(StartupAdPhase.Showing, gate.phase.value)
        gate.finish()
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
        assertFalse(gate.startLoading())
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
    }

    @Test fun contentReadyWithoutAdPermanentlyRejectsLateLoadsAndResumeShows() {
        val gate = StartupAdGate()
        gate.startLoading()
        assertFalse(gate.onAppReady(adAvailable = false, eligible = true))
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
        assertFalse(gate.acceptsLoadedAd())
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
        assertFalse(gate.startLoading())
    }

    @Test fun backgroundOrConsentFlowCannotShowAnAdEvenWhenItIsReady() {
        val gate = StartupAdGate()
        gate.startLoading()
        assertFalse(gate.onAppReady(adAvailable = true, eligible = false))
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
    }

    @Test fun restoreOrLoadFailureClosesPendingStartupPermanently() {
        val gate = StartupAdGate()
        gate.startLoading()
        gate.finish()
        assertFalse(gate.acceptsLoadedAd())
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
        assertFalse(gate.startLoading())
    }

    @Test fun restoredContentWithoutARequestCannotAcquireAnAdOpportunity() {
        val gate = StartupAdGate()
        assertFalse(gate.onAppReady(adAvailable = true, eligible = true))
        assertFalse(gate.startLoading())
        assertEquals(StartupAdPhase.Finished, gate.phase.value)
    }
}
