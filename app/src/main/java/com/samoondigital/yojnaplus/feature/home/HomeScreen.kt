package com.samoondigital.yojnaplus.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import com.samoondigital.yojnaplus.core.ui.theme.IndigoDark
import com.samoondigital.yojnaplus.core.ui.theme.Indigo

@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    onOpenDownloads: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHero(
            onOpenVoterList = onDownloadPdf,
        )

        Column(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SavedPdfCard(onOpenDownloads)
            HowToDownload()
            HomeDisclaimer()
            Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 8.dp))
        }
    }
}

@Composable
private fun HomeHero(
    onOpenVoterList: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(IndigoDark, Indigo),
                ),
            )
            .padding(start = 21.dp, end = 21.dp, top = 10.dp, bottom = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding(),
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ui_home_help),
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onOpenVoterList,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                Text(
                    stringResource(R.string.ui_download_list),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SavedPdfCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ui_saved_pdfs), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.ui_saved_help), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HowToDownload() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.ui_how_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.ui_how_steps), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeDisclaimer() {
    val uriHandler = LocalUriHandler.current
    val privacyUrl = "https://sites.google.com/view/voterlist2026/home"
    val instagramUrl = "https://www.instagram.com/samoon_digital/"
    val email = "alimailsamun@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = stringResource(R.string.ui_disclaimer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                Text(stringResource(R.string.ui_privacy))
            }
            TextButton(onClick = { uriHandler.openUri(instagramUrl) }) {
                Text(stringResource(R.string.ui_follow))
            }
            TextButton(onClick = { uriHandler.openUri("mailto:$email") }) {
                Text(stringResource(R.string.ui_contact))
            }
        }
    }
}
