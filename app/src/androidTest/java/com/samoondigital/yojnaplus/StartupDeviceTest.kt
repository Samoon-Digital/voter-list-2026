package com.samoondigital.yojnaplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.ads.StartupAdPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StartupDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeRemainsUsableAfterBackgroundAndActivityRecreation() {
        assertHomeReady()
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        assertHomeReady()
        compose.activityRule.scenario.recreate()
        assertHomeReady()
    }

    private fun assertHomeReady() {
        compose.waitUntil(10_000) { AppOpenAdManager.startupPhase.value == StartupAdPhase.Finished }
        compose.onNodeWithText(compose.activity.getString(R.string.app_name)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(StartupAdPhase.Finished, AppOpenAdManager.startupPhase.value) }
    }
}
