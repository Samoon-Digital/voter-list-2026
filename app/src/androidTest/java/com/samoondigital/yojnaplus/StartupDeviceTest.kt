package com.samoondigital.yojnaplus

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.ads.StartupAdPhase
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StartupDeviceTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun homeRemainsUsableAfterBackgroundAndActivityRecreation() {
        // Keep UI lifecycle checks independent of remote ad creatives and their dismiss controls.
        // Deadline/late-ad behavior is covered by StartupAdGateTest; SDK show/dismiss gets a device smoke check.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppOpenAdManager.skipStartup("lifecycle-test")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertHomeReady()
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertHomeReady()
            scenario.recreate()
            assertHomeReady()
            val workManager = WorkManager.getInstance(context)
            compose.waitUntil(5_000) {
                workManager.getWorkInfosForUniqueWork("appscope-recovery").get(1, TimeUnit.SECONDS).isNotEmpty()
            }
        }
    }

    private fun assertHomeReady() {
        val heading = context.getString(R.string.ui_home_heading)
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(heading).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(heading).assertIsDisplayed()
        compose.runOnIdle { assertEquals(StartupAdPhase.Finished, AppOpenAdManager.startupPhase.value) }
    }
}
