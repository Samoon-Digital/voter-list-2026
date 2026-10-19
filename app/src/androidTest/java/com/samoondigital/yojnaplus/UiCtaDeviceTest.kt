package com.samoondigital.yojnaplus

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
import com.samoondigital.yojnaplus.feature.pdf.PartsStep
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
        compose.onNodeWithText(context.getString(R.string.ui_download_list)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.ui_saved_pdfs)).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, downloadClicks)
            assertEquals(1, savedClicks)
        }
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
                PartsStep(
                    uiState = state.value,
                    onPartToggled = { part -> state.value = state.value.copy(selectedPartNumbers = setOf(part)) },
                    onProceed = { proceedClicks++ },
                    onChangeSelection = {},
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.ui_proceed, 0)).assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText("Uttar Pradesh / 2026 / Rampur / Rampur").assertIsDisplayed()
        compose.onNodeWithText("प्राथमिक विद्यालय, रामपुर").performClick()
        compose.onNodeWithText(context.getString(R.string.ui_proceed, 1)).assertIsDisplayed().assertIsEnabled().performClick()
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
                    onDownloadList = { downloadClicks++ }, bottomContentPadding = 100.dp,
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
        compose.onNodeWithText(context.getString(R.string.ui_search_in_pdf)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.ui_share)).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, searchClicks)
            assertEquals(1, shareClicks)
        }
        saveScreenshot("pdf-toolbar-large-font")
    }

    private fun saveScreenshot(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(context.getExternalFilesDir(null), "ui-review").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}