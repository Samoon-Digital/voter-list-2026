package com.samoondigital.yojnaplus.feature.pdf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.core.ui.components.PlaceholderContent

/**
 * Voter List PDF screen. Heavy resources (PDF renderer / WebView) must be
 * created here and released in onDispose so memory is freed on navigation away.
 */
@Composable
fun PdfScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { AppToolbar(title = "Voter List PDF", onBack = onBack) },
    ) { padding ->
        PlaceholderContent(
            icon = Icons.Outlined.PictureAsPdf,
            title = "District / Assembly PDF",
            subtitle = "Select district, assembly and part to download the latest electoral roll PDF.",
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
