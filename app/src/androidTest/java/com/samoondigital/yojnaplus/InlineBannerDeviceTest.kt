package com.samoondigital.yojnaplus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import com.samoondigital.yojnaplus.ads.InlineBanner
import com.samoondigital.yojnaplus.ads.InlineBannerState
import com.samoondigital.yojnaplus.ads.rememberInlineBanner
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.feature.pdf.PartsStep
import com.samoondigital.yojnaplus.feature.pdf.StateStep
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InlineBannerDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun bannerFollowsFifthRowOrShortListAndDisappearsForEmptySearch() {
        val states = mutableStateOf((1..7).map { StateDto("S$it", "State $it") })
        var selected = ""
        compose.setContent {
            VoterList2026Theme {
                StateStep(
                    ElectoralRollUiState(states = states.value),
                    inlineBanner = { Box(Modifier.fillMaxWidth().height(90.dp).testTag("test-banner")) },
                    onSelected = { selected = it.stateCd },
                )
            }
        }
        compose.onNodeWithTag("test-banner").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("test-banner").assertCountEquals(1)
        val fifth = compose.onNodeWithText("State 5").fetchSemanticsNode().boundsInRoot
        val banner = compose.onNodeWithTag("test-banner").fetchSemanticsNode().boundsInRoot
        assertTrue(fifth.bottom <= banner.top)
        compose.onNodeWithText("State 6").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("S6", selected) }

        compose.onNode(hasSetTextAction()).performTextReplacement("State 1")
        compose.onNodeWithTag("test-banner").assertIsDisplayed()
        assertTrue(compose.onNode(hasText("State 1") and !hasSetTextAction()).fetchSemanticsNode().boundsInRoot.bottom <=
            compose.onNodeWithTag("test-banner").fetchSemanticsNode().boundsInRoot.top)
        compose.onNode(hasSetTextAction()).performTextReplacement("no matching state")
        compose.onAllNodesWithTag("test-banner").assertCountEquals(0)
        compose.onNode(hasSetTextAction()).performTextReplacement("")
        compose.runOnIdle { states.value = emptyList() }
        compose.onAllNodesWithTag("test-banner").assertCountEquals(0)
    }

    @Test
    fun partsBannerLeavesSelectionAndContinueUsable() {
        val state = mutableStateOf(ElectoralRollUiState(
            parts = (1..20).map { PartDto(partNumber = it, partName = "Centre $it") },
        ))
        var continued = false
        compose.setContent {
            VoterList2026Theme {
                PartsStep(
                    state.value,
                    onPartToggled = { state.value = state.value.copy(selectedPartNumbers = setOf(it)) },
                    onProceed = { continued = true },
                    inlineBanner = { Text("Test banner", Modifier.height(90.dp)) },
                )
            }
        }
        compose.onNodeWithText("Test banner").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Centre 6").performScrollTo().performClick()
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        compose.onNodeWithText(context.getString(R.string.ui_continue)).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(setOf(6), state.value.selectedPartNumbers)
            assertTrue(continued)
        }
    }

    @Test
    fun bannerViewSurvivesHostChangesAndIsDestroyedWhenRouteLeaves() {
        val host = mutableStateOf(0)
        val routeVisible = mutableStateOf(true)
        lateinit var owner: InlineBannerState
        compose.setContent {
            VoterList2026Theme {
                if (routeVisible.value) {
                    val banner = rememberInlineBanner(320)
                    owner = banner
                    androidx.compose.runtime.key(host.value) { InlineBanner(banner) }
                }
            }
        }
        lateinit var view: com.google.android.libraries.ads.mobile.sdk.banner.AdView
        compose.runOnIdle {
            assertNotNull(owner.view)
            view = owner.view!!
            host.value++
        }
        compose.runOnIdle {
            assertSame(view, owner.view)
            routeVisible.value = false
        }
        compose.runOnIdle {
            assertNull(owner.view)
            assertNull(view.parent)
        }
    }
}
