package com.samoondigital.yojnaplus

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.EditText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.SdkSuppress
import com.samoondigital.yojnaplus.feature.pdfviewer.AppPdfViewerFragment
import java.io.File
import java.util.UUID
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 31)
class NativePdfSearchDeviceTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun nativeSearchFindsTextClosesAndSurvivesActivityRecreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "p1-search-${UUID.randomUUID()}.pdf")
        val document = PdfDocument()
        try {
            repeat(2) { index ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(600, 800, index + 1).create())
                page.canvas.drawText("Voter List 2026", 40f, 100f, Paint().apply { textSize = 24f })
                document.finishPage(page)
            }
            file.outputStream().use(document::writeTo)
        } finally { document.close() }
        try {
            val intent = Intent(context, PdfViewerTestActivity::class.java).putExtra("uri", Uri.fromFile(file).toString())
            ActivityScenario.launch<PdfViewerTestActivity>(intent).use { scenario ->
                waitForDocument(scenario)
                openSearch(context)
                onView(isAssignableFrom(EditText::class.java)).perform(typeText("Voter"), closeSoftKeyboard())
                compose.waitUntil(15_000) {
                    runCatching { onView(withText("1 / 2")).check(matches(isDisplayed())) }.isSuccess
                }
                onView(withContentDescription("Next")).perform(click())
                onView(withText("2 / 2")).check(matches(isDisplayed()))
                onView(withContentDescription("Close")).perform(click())
                scenario.onActivity { activity -> assertFalse(fragment(activity).isTextSearchActive) }
                scenario.recreate()
                waitForDocument(scenario)
                openSearch(context)
                // Once the keyboard is hidden, Back closes search and keeps the PDF open.
                onView(isAssignableFrom(EditText::class.java)).perform(closeSoftKeyboard())
                androidx.test.espresso.Espresso.pressBack()
                scenario.onActivity { activity ->
                    assertFalse(fragment(activity).isTextSearchActive)
                    assertFalse(activity.isFinishing)
                }
            }
        } finally { file.delete() }
    }

    private fun openSearch(context: Context) {
        compose.onNodeWithContentDescription(context.getString(R.string.ui_search_in_pdf)).performClick()
        onView(isAssignableFrom(EditText::class.java)).check(matches(isDisplayed()))
    }

    private fun waitForDocument(scenario: ActivityScenario<PdfViewerTestActivity>) {
        compose.waitUntil(30_000) {
            var ready = false
            scenario.onActivity { activity ->
                ready = activity.supportFragmentManager.fragments.filterIsInstance<AppPdfViewerFragment>()
                    .firstOrNull()?.isDocumentReady == true
            }
            ready
        }
    }

    private fun fragment(activity: PdfViewerTestActivity): AppPdfViewerFragment =
        activity.supportFragmentManager.fragments.filterIsInstance<AppPdfViewerFragment>().single()
}
