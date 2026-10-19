package com.samoondigital.yojnaplus.feature.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.ads.ConsentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    onOpenDownloads: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppToolbar(title = stringResource(R.string.app_name)) {
            IconButton(onClick = { showHelp = true }) {
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = stringResource(R.string.ui_help),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.ui_home_heading),
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            DownloadListCard(onDownloadPdf)
            SavedPdfCard(onOpenDownloads)
            Surface(
                onClick = { showHelp = true },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.ui_how_title), style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        HomeDisclaimer()
        Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 4.dp))
    }
    if (showHelp) {
        ModalBottomSheet(onDismissRequest = { showHelp = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.ui_how_title), style = MaterialTheme.typography.titleLarge)
                listOf(R.string.ui_guide_list, R.string.ui_guide_area, R.string.ui_guide_pdf).forEachIndexed { index, label ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${index + 1}.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DownloadListCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.drawWithCache {
                val gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF4430B4), Color(0xFF4D38C4), Color(0xFF5944D0)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                onDrawBehind {
                    drawRect(gradient)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.035f),
                        radius = size.width * 0.55f,
                        center = Offset(size.width * 1.16f, size.height * 1.5f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.025f),
                        radius = size.width * 0.48f,
                        center = Offset(size.width * 1.23f, size.height * 0.25f),
                    )
                }
            }.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(30.dp))
                Text(stringResource(R.string.ui_home_download_list), style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.ui_home_start_hint), style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun SavedPdfCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp).size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ui_saved_pdfs), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.ui_saved_help), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeDisclaimer() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val privacyOptionsRequired by ConsentManager.privacyOptionsRequired.collectAsStateWithLifecycle()
    val privacyUrl = "https://sites.google.com/view/voterlist2026/home"
    val instagramUrl = "https://www.instagram.com/samoon_digital/"
    val email = "alimailsamun@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = stringResource(R.string.ui_disclaimer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            textAlign = TextAlign.Center,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                Text(stringResource(R.string.ui_privacy), style = MaterialTheme.typography.labelMedium)
            }
            PrivacyChoicesButton(required = privacyOptionsRequired, onClick = {
                context.findActivity()?.let { activity ->
                    ConsentManager.showPrivacyOptions(activity) {
                        Toast.makeText(context, R.string.ui_privacy_options_error, Toast.LENGTH_SHORT).show()
                    }
                }
            })
            TextButton(onClick = { uriHandler.openUri(instagramUrl) }) {
                Text(stringResource(R.string.ui_follow), style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = { uriHandler.openUri("mailto:$email") }) {
                Text(stringResource(R.string.ui_contact), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun PrivacyChoicesButton(required: Boolean, onClick: () -> Unit) {
    if (required) {
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.ui_privacy_choices), style = MaterialTheme.typography.labelMedium)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
