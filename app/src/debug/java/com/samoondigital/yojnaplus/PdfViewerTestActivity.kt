package com.samoondigital.yojnaplus

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import com.samoondigital.yojnaplus.core.ui.theme.VoterList2026Theme
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerScreen
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerViewModel

/** Debug-only host for exercising the real PDF screen without modifying the user's downloads. */
class PdfViewerTestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewer = PdfViewerViewModel(this, SavedStateHandle(mapOf(
            "uri" to intent.getStringExtra("uri").orEmpty(), "title" to "PDF search test",
        )))
        setContent {
            VoterList2026Theme { PdfViewerScreen(onBack = { finish() }, viewModel = viewer) }
        }
    }
}
