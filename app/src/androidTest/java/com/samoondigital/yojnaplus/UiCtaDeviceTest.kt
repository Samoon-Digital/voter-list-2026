package com.samoondigital.yojnaplus

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.feature.downloads.DownloadsContent
import com.samoondigital.yojnaplus.feature.downloads.DownloadsUiState
import com.samoondigital.yojnaplus.feature.downloads.DownloadItemUi
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadStatusEntity
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerTopBar
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.model.DistrictDto
import com.samoondigital.yojnaplus.model.AssemblyDto
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.home.PrivacyChoicesButton
import com.samoondigital.yojnaplus.feature.pdf.PartsStep
import com.samoondigital.yojnaplus.feature.pdf.StateStep
import com.samoondigital.yojnaplus.feature.pdf.SelectionToolbar
import com.samoondigital.yojnaplus.feature.pdf.CaptchaStep
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollStep
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performScrollTo
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollUiState
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UiCtaDeviceTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun privacyChoicesEntryIsVisibleAndClickableOnlyWhenRequired() {
        val required = mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            VoterList2026Theme {
                PrivacyChoicesButton(required = required.value, onClick = { clicks++ })
            }
        }
        val label = context.getString(R.string.ui_privacy_choices)
        compose.onNodeWithText(label).assertDoesNotExist()
        compose.runOnIdle { required.value = true }
        compose.onNodeWithText(label).assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, clicks); required.value = false }
        compose.onNodeWithText(label).assertDoesNotExist()
    }

    @Test
    fun homeActionsRemainVisibleAtLargeFontAndOpenCorrectDestinations() {
        var downloadClicks = 0
        var savedClicks = 0
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1.5f)) {
                VoterList2026Theme {
                    HomeScreen(
                        onDownloadPdf = { downloadClicks++ },
                        onOpenDownloads = { savedClicks++ },
                        contentPadding = PaddingValues(),
                    )
                }
            }
        }
        compose.onNodeWithTag("app-toolbar").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.ui_home_download_list)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.ui_saved_pdfs)).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, downloadClicks)
            assertEquals(1, savedClicks)
        }
        compose.onNodeWithText(context.getString(R.string.ui_how_title)).performClick()
        compose.onNodeWithText(context.getString(R.string.ui_guide_list)).assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        saveScreenshot("home-large-font")
    }

    @Test
    fun pollingCentreSelectionEnablesThePersistentContinueAction() {
        val state = mutableStateOf(ElectoralRollUiState(
            selectedState = StateDto("S24", "Uttar Pradesh"),
            selectedYear = 2026,
            selectedDistrict = DistrictDto("S2401", districtName = "Rampur"),
            selectedAssembly = AssemblyDto(12, "Rampur"),
            parts = listOf(PartDto(partNumber = 12, partName = "प्राथमिक विद्यालय, रामपुर")),
        ))
        var proceedClicks = 0
        compose.setContent {
            VoterList2026Theme {
                Column(Modifier.fillMaxSize()) {
                    SelectionToolbar(state.value.copy(step = ElectoralRollStep.Parts), onBack = {}, onOpenDownloads = {})
                    Box(Modifier.weight(1f)) {
                        PartsStep(
                            uiState = state.value,
                            onPartToggled = { part -> state.value = state.value.copy(selectedPartNumbers = setOf(part)) },
                            onProceed = { proceedClicks++ },
                        )
                    }
                }
            }
        }
        compose.onNodeWithTag("app-toolbar").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.ui_continue)).assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.ui_selected_details)).assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.ui_options)).performClick()
        compose.onNodeWithText(context.getString(R.string.ui_show_selection)).performClick()
        compose.onNodeWithText("Uttar Pradesh / 2026 / Rampur / Rampur").assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithText("प्राथमिक विद्यालय, रामपुर").performClick()
        compose.onNodeWithText(context.getString(R.string.ui_continue)).assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, proceedClicks) }
        saveScreenshot("polling-centre-selected")
    }

    @Test
    fun downloadsShowOpenShareAndUsefulEmptyActions() {
        val record = DownloadItemUi(
            id = "test-pdf", fileName = "Rampur Part 12.pdf", district = "Rampur", assembly = "Rampur",
            village = "प्राथमिक विद्यालय", fileSize = "1 MB", downloadedAt = "19 Oct 2026",
            status = DownloadStatusEntity.Completed, progress = 100, errorMessage = null,
        )
        val state = mutableStateOf(DownloadsUiState(downloads = listOf(record)))
        var openClicks = 0
        var shareClicks = 0
        var downloadClicks = 0
        compose.setContent {
            VoterList2026Theme {
                DownloadsContent(
                    state = state.value, onSearch = {}, onSync = {},
                    onOpen = { assertEquals("test-pdf", it); openClicks++ },
                    onShare = { assertEquals("test-pdf", it); shareClicks++ },
                    onRename = {}, onDelete = {}, onRetry = {}, onCancel = {},
                    onDownloadList = { downloadClicks++ },
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.ui_open_pdf)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.ui_share)).assertIsDisplayed().performClick()
        saveScreenshot("downloads-actions")
        compose.runOnIdle {
            assertEquals(1, openClicks)
            assertEquals(1, shareClicks)
            state.value = DownloadsUiState()
        }
        compose.onNodeWithText(context.getString(R.string.ui_download_list)).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, downloadClicks) }
        saveScreenshot("downloads-empty")
    }

    @Test
    fun pdfToolbarExposesSearchAndShareAtLargeFont() {
        var searchClicks = 0
        var shareClicks = 0
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.5f)) {
                VoterList2026Theme {
                    PdfViewerTopBar(
                        title = "Rampur Part 12.pdf", pageLabel = "1 / 10", onBack = {},
                        onSearch = { searchClicks++ }, onShare = { shareClicks++ },
                    )
                }
            }
        }
        compose.onNodeWithTag("app-toolbar").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.ui_search_in_pdf)).assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.ui_share)).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, searchClicks)
            assertEquals(1, shareClicks)
        }
        saveScreenshot("pdf-toolbar-large-font")
    }

    @Test
    fun stateSearchSupportsSpacingAndClearInDarkTheme() {
        var chosen = ""
        compose.setContent {
            VoterList2026Theme(darkTheme = true) {
                Column {
                    SelectionToolbar(ElectoralRollUiState(), onBack = {}, onOpenDownloads = {})
                    StateStep(ElectoralRollUiState(states = listOf(
                        StateDto("S24", "Uttar Pradesh"), StateDto("S04", "Bihar"),
                    ))) { chosen = it.stateName }
                }
            }
        }
        compose.onNodeWithTag("app-toolbar").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("uttar   pradesh")
        compose.onNodeWithText("Bihar").assertDoesNotExist()
        compose.onNodeWithText("Uttar Pradesh").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("Uttar Pradesh", chosen) }
        compose.onNodeWithContentDescription(context.getString(R.string.ui_clear_search)).performClick()
        compose.onNodeWithText("Bihar").assertIsDisplayed()
        saveScreenshot("state-search-dark")
    }

    @Test
    fun captchaKeepsDownloadDisabledWhileCodeIsUnavailableAndRefreshIsReachable() {
        var refreshClicks = 0
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.5f)) {
                VoterList2026Theme {
                    CaptchaStep(
                        uiState = ElectoralRollUiState(selectedPartNumbers = setOf(12)),
                        onCaptchaChanged = {}, onRefreshCaptcha = { refreshClicks++ },
                        onStartDownload = {}, onRetry = {}, onCancel = {},
                    )
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.ui_new_code)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.ui_download_count, 1))
            .performScrollTo().assertIsDisplayed().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, refreshClicks) }
        saveScreenshot("captcha-large-font")
    }

    @Test
    fun pollingCentreListGetsMostOfTheScreenAndShowsMoreThanThreeRows() {
        compose.setContent {
            VoterList2026Theme {
                val state = ElectoralRollUiState(
                    step = ElectoralRollStep.Parts,
                    parts = (1..30).map { PartDto(partNumber = it, partName = "मतदान केंद्र $it") },
                )
                Column(Modifier.fillMaxSize()) {
                    SelectionToolbar(state, onBack = {}, onOpenDownloads = {})
                    Box(Modifier.weight(1f)) {
                        PartsStep(state, onPartToggled = {}, onProceed = {})
                    }
                }
            }
        }
        val screen = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val list = compose.onNodeWithTag("selection-list").fetchSemanticsNode().boundsInRoot
        val ratio = list.height / screen.height
        assertTrue("List uses only ${ratio * 100}% of the screen", ratio >= 0.70f)
        compose.onNodeWithText("मतदान केंद्र 6").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.ui_parts_help)).assertDoesNotExist()
        saveScreenshot("parts-list-space")
    }

    private fun saveScreenshot(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(context.getExternalFilesDir(null), "ui-review").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
