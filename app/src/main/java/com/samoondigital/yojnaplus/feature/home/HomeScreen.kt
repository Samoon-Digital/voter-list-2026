package com.samoondigital.yojnaplus.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.FeatureCard
import com.samoondigital.yojnaplus.core.ui.theme.Green
import com.samoondigital.yojnaplus.core.ui.theme.GreenContainer
import com.samoondigital.yojnaplus.core.ui.theme.Indigo
import com.samoondigital.yojnaplus.core.ui.theme.OnGreenContainer

/**
 * Home landing screen. Pure UI built from the supplied mockup; navigation is
 * delegated upward via callbacks so the screen stays testable and ViewModel-free
 * where no state is required.
 */
@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = Indigo,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(28.dp))

        FeatureCard(
            title = stringResource(R.string.voter_list_pdf),
            description = stringResource(R.string.voter_list_pdf_desc),
            actionLabel = stringResource(R.string.download_pdf),
            icon = Icons.Outlined.Download,
            accent = Green,
            container = GreenContainer,
            onAction = Color.White,
            onContainer = OnGreenContainer,
            onClick = onDownloadPdf,
        )

        Spacer(Modifier.height(24.dp))
    }
}
