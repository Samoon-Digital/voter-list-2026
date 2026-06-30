package com.samoondigital.yojnaplus.feature.pdf

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.model.AssemblyDto
import com.samoondigital.yojnaplus.model.DistrictDto
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.RollTypeDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.pdf.DownloadedPdf
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollUiState
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollViewModel

@Composable
fun PdfScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectoralRollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppToolbar(title = "Voter List PDF", onBack = onBack) },
    ) { padding ->
        PdfStepForm(
            uiState = uiState,
            onYearSelected = viewModel::selectYear,
            onStateSelected = viewModel::selectState,
            onRollTypeSelected = viewModel::selectRollType,
            onDistrictSelected = viewModel::selectDistrict,
            onAssemblySelected = viewModel::selectAssembly,
            onLanguageSelected = viewModel::selectLanguage,
            onPartToggled = viewModel::togglePart,
            onCaptchaChanged = viewModel::updateCaptchaInput,
            onRefreshCaptcha = viewModel::refreshCaptcha,
            onDownload = viewModel::downloadSelectedPdfs,
            onOpenPdf = viewModel::openPdf,
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun PdfStepForm(
    uiState: ElectoralRollUiState,
    onYearSelected: (Int) -> Unit,
    onStateSelected: (StateDto) -> Unit,
    onRollTypeSelected: (RollTypeDto) -> Unit,
    onDistrictSelected: (DistrictDto) -> Unit,
    onAssemblySelected: (AssemblyDto) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onPartToggled: (Int) -> Unit,
    onCaptchaChanged: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onDownload: () -> Unit,
    onOpenPdf: (DownloadedPdf) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            ProgressHeader(uiState)
        }

        item {
            StepSection(step = 1, title = "State and Revision") {
                SelectorField(
                    label = "State",
                    value = uiState.selectedState?.stateName ?: "Select State",
                    items = uiState.states,
                    itemLabel = StateDto::stateName,
                    onItemSelected = onStateSelected,
                    enabled = !uiState.isLoading && !uiState.isDownloading,
                )
                Spacer(Modifier.height(10.dp))
                SelectorField(
                    label = "Year of Revision",
                    value = uiState.selectedYear.toString(),
                    items = uiState.years,
                    itemLabel = Int::toString,
                    onItemSelected = onYearSelected,
                    enabled = !uiState.isLoading && !uiState.isDownloading,
                )
            }
        }

        item {
            StepSection(step = 2, title = "Roll Type") {
                SelectorField(
                    label = "Roll Type",
                    value = uiState.selectedRollType?.displayName ?: "Select Roll Type",
                    items = uiState.rollTypes,
                    itemLabel = RollTypeDto::displayName,
                    onItemSelected = onRollTypeSelected,
                    enabled = uiState.selectedState != null && !uiState.isLoading && !uiState.isDownloading,
                )
            }
        }

        item {
            StepSection(step = 3, title = "District and Assembly") {
                SelectorField(
                    label = "District",
                    value = uiState.selectedDistrict?.displayName ?: "Select District",
                    items = uiState.districts,
                    itemLabel = DistrictDto::displayName,
                    onItemSelected = onDistrictSelected,
                    enabled = uiState.selectedRollType != null && !uiState.isLoading && !uiState.isDownloading,
                )
                Spacer(Modifier.height(10.dp))
                SelectorField(
                    label = "Assembly Constituency",
                    value = uiState.selectedAssembly?.let { "${it.asmblyNo} - ${it.asmblyName}" } ?: "Select AC",
                    items = uiState.assemblies,
                    itemLabel = { "${it.asmblyNo} - ${it.asmblyName}" },
                    onItemSelected = onAssemblySelected,
                    enabled = uiState.selectedDistrict != null && !uiState.isLoading && !uiState.isDownloading,
                )
            }
        }

        item {
            StepSection(step = 4, title = "Language") {
                SelectorField(
                    label = "Language",
                    value = uiState.selectedLanguageCode?.let { code ->
                        "${uiState.languages[code].orEmpty()} ($code)"
                    } ?: "Select Language",
                    items = uiState.languages.entries.toList(),
                    itemLabel = { "${it.value} (${it.key})" },
                    onItemSelected = { onLanguageSelected(it.key) },
                    enabled = uiState.languages.isNotEmpty() && !uiState.isDownloading,
                )
            }
        }

        item {
            StepSection(step = 5, title = "Select Parts") {
                PartsSummary(uiState)
            }
        }

        items(uiState.parts, key = { it.partNumber }) { part ->
            PartRow(
                part = part,
                checked = part.partNumber in uiState.selectedPartNumbers,
                enabled = !uiState.isDownloading,
                onCheckedChange = { onPartToggled(part.partNumber) },
            )
        }

        item {
            StepSection(step = 6, title = "Captcha") {
                CaptchaBlock(
                    uiState = uiState,
                    onCaptchaChanged = onCaptchaChanged,
                    onRefreshCaptcha = onRefreshCaptcha,
                )
            }
        }

        item {
            Button(
                onClick = onDownload,
                enabled = !uiState.isDownloading && !uiState.isLoading,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download Selected PDFs")
            }
        }

        item {
            AnimatedVisibility(visible = uiState.downloadedPdfs.isNotEmpty()) {
                DownloadedList(
                    downloads = uiState.downloadedPdfs,
                    onOpenPdf = onOpenPdf,
                )
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProgressHeader(uiState: ElectoralRollUiState) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Electoral Roll PDF",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Step ${uiState.currentStep}/8",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            LinearProgressIndicator(
                progress = { uiState.currentStep / 8f },
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(visible = uiState.isLoading || uiState.isDownloading || uiState.message != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    if (uiState.isDownloading) {
                        LinearProgressIndicator(
                            progress = { uiState.downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSection(
    step: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Step $step",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun <T> SelectorField(
    label: String,
    value: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled && items.isNotEmpty(),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp),
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun PartsSummary(uiState: ElectoralRollUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Polling Station Parts",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "${uiState.selectedPartNumbers.size}/10",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = if (uiState.parts.isEmpty()) "Select assembly to load part numbers" else "${uiState.parts.size} parts available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PartRow(
    part: PartDto,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: () -> Unit,
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onCheckedChange() },
                enabled = enabled,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Part ${part.partNumber}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = part.partName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CaptchaBlock(
    uiState: ElectoralRollUiState,
    onCaptchaChanged: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CaptchaImage(uiState.captcha?.captcha)
            IconButton(
                onClick = onRefreshCaptcha,
                enabled = !uiState.isCaptchaLoading && !uiState.isDownloading,
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh captcha")
            }
        }
        OutlinedTextField(
            value = uiState.captchaInput,
            onValueChange = onCaptchaChanged,
            enabled = !uiState.isDownloading,
            label = { Text("Enter Captcha") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CaptchaImage(base64Captcha: String?) {
    val bitmap = remember(base64Captcha) {
        runCatching {
            val bytes = Base64.decode(base64Captcha.orEmpty(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .size(width = 132.dp, height = 50.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captcha",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = "Captcha",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DownloadedList(
    downloads: List<DownloadedPdf>,
    onOpenPdf: (DownloadedPdf) -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Downloaded PDFs",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            downloads.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onOpenPdf(item) }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "Open PDF")
                    }
                }
            }
        }
    }
}
