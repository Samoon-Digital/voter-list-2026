package com.samoondigital.yojnaplus.feature.pdf

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.model.AssemblyDto
import com.samoondigital.yojnaplus.model.DistrictDto
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.RollTypeDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.viewmodel.DownloadStatus
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollDownloadItem
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollStep
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollUiState
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollViewModel


private const val GeneralDistrictPlacement = "general-voter-list-district-to-assembly"
private const val GeneralLanguagePlacement = "general-voter-list-language-to-captcha"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PdfScreen(
    onBack: () -> Unit,
    onDownloadsComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectoralRollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    var openedDownloads by remember { mutableStateOf(false) }

    fun handleBack() {
        if (!viewModel.goBack()) onBack()
    }

    BackHandler(onBack = ::handleBack)
    LaunchedEffect(Unit) {
        InterstitialAdManager.preload(context.applicationContext)
    }
    LaunchedEffect(uiState.step, uiState.isDownloading, uiState.completedCount, uiState.failedCount) {
        if (
            !openedDownloads &&
            uiState.step == ElectoralRollStep.Success &&
            !uiState.isDownloading &&
            uiState.completedCount > 0 &&
            uiState.failedCount == 0
        ) {
            openedDownloads = true
            onDownloadsComplete()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WizardTopBar(
                uiState = uiState,
                onBack = ::handleBack,
                onOpenDownloads = onDownloadsComplete,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "electoral-roll-step",
            ) { step ->
                when (step) {
                    ElectoralRollStep.State -> StateStep(uiState, viewModel::selectState)
                    ElectoralRollStep.Year -> YearStep(uiState, viewModel::selectYear)
                    ElectoralRollStep.RollType -> RollTypeStep(uiState, viewModel::selectRollType)
                    ElectoralRollStep.District -> DistrictStep(uiState, onChangeSelection = ::handleBack) { district ->
                        InterstitialAdManager.showIfAvailable(
                            activity = activity,
                            placement = GeneralDistrictPlacement,
                        ) {
                            viewModel.selectDistrict(district)
                        }
                    }
                    ElectoralRollStep.Assembly -> AssemblyStep(uiState, viewModel::selectAssembly, ::handleBack)
                    ElectoralRollStep.Parts -> PartsStep(
                        uiState = uiState,
                        onPartToggled = viewModel::togglePart,
                        onProceed = viewModel::showLanguageSheet,
                        onChangeSelection = ::handleBack,
                    )
                    ElectoralRollStep.Captcha -> CaptchaStep(
                        uiState = uiState,
                        onCaptchaChanged = viewModel::updateCaptchaInput,
                        onRefreshCaptcha = viewModel::refreshCaptcha,
                        onStartDownload = viewModel::startDownloads,
                        onRetry = viewModel::retryDownloads,
                        onCancel = viewModel::cancelDownloads,
                    )
                    ElectoralRollStep.Success -> SuccessStep(
                        uiState = uiState,
                        onOpenDownloads = onDownloadsComplete,
                    )
                }
            }
        }
    }

    if (uiState.isLanguageSheetVisible) {
        LanguageBottomSheet(
            uiState = uiState,
            onDismiss = viewModel::dismissLanguageSheet,
            onSelected = { languageCode ->
                InterstitialAdManager.showIfAvailable(
                    activity = activity,
                    placement = GeneralLanguagePlacement,
                ) {
                    viewModel.selectLanguage(languageCode)
                }
            },
        )
    }
}

@Composable
private fun WizardTopBar(
    uiState: ElectoralRollUiState,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    Column {
        AppToolbar(title = stringResource(R.string.ui_wizard_title), onBack = onBack) {
            IconButton(onClick = onOpenDownloads) {
                Icon(Icons.Outlined.FileDownload, contentDescription = stringResource(R.string.ui_saved_pdfs))
            }
        }
        SelectionProgress(uiState.step)
    }
}

@Composable
internal fun SelectionProgress(step: ElectoralRollStep) {
    val activeGroup = when (step) {
        ElectoralRollStep.State, ElectoralRollStep.Year, ElectoralRollStep.RollType -> 0
        ElectoralRollStep.District, ElectoralRollStep.Assembly, ElectoralRollStep.Parts -> 1
        ElectoralRollStep.Captcha, ElectoralRollStep.Success -> 2
    }
    val groupProgress = when (step) {
        ElectoralRollStep.State, ElectoralRollStep.District -> 0f
        ElectoralRollStep.Year, ElectoralRollStep.Assembly -> 1f / 3f
        ElectoralRollStep.RollType, ElectoralRollStep.Parts -> 2f / 3f
        ElectoralRollStep.Captcha -> 0f
        ElectoralRollStep.Success -> 1f
    }
    val labels = listOf(R.string.ui_group_list, R.string.ui_group_area, R.string.ui_group_pdf)
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { index, label ->
                val reached = index <= activeGroup
                val status = stringResource(when {
                    index < activeGroup || step == ElectoralRollStep.Success -> R.string.ui_group_complete
                    index == activeGroup -> R.string.ui_group_current
                    else -> R.string.ui_group_upcoming
                })
                Column(Modifier.weight(1f).semantics(mergeDescendants = true) { stateDescription = status },
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape,
                            color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (reached) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
                            Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                                if (index < activeGroup || step == ElectoralRollStep.Success) Icon(Icons.Outlined.Check, contentDescription = stringResource(R.string.ui_group_complete), modifier = Modifier.size(14.dp))
                                else Text((index + 1).toString(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(stringResource(label), style = MaterialTheme.typography.labelMedium,
                            color = if (index == activeGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f))
                    }
                    val progress = if (index < activeGroup) 1f else if (index == activeGroup) groupProgress else 0f
                    Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)) {
                        Box(Modifier.fillMaxWidth(progress).height(3.dp).background(MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }
    }
}

@Composable
internal fun StateStep(uiState: ElectoralRollUiState, onSelected: (StateDto) -> Unit) {
    SearchableChoiceScreen(
        title = stringResource(R.string.ui_state_title),
        subtitle = stringResource(R.string.ui_state_help),
        queryPlaceholder = stringResource(R.string.ui_search_state),
        items = uiState.states,
        itemTitle = StateDto::stateName,
        itemSubtitle = { null },
        headerIcon = Icons.Outlined.Map,
        itemIcon = Icons.Outlined.Map,
        loading = uiState.isLoading && uiState.states.isEmpty(),
        message = uiState.message,
        showSearch = true,
        onSelected = onSelected,
    )
}

@Composable
private fun YearStep(uiState: ElectoralRollUiState, onSelected: (Int) -> Unit) {
    ChoiceListScaffold(
        title = stringResource(R.string.ui_year_title),
        subtitle = stringResource(R.string.ui_year_help, uiState.selectedState?.stateName.orEmpty()),
        headerIcon = Icons.Outlined.CalendarMonth,
        loading = uiState.isLoading,
        message = uiState.message,
    ) { _ ->
        items(uiState.years, key = { it }) { year ->
            ChoiceCard(
                title = year.toString(),
                subtitle = null,
                icon = Icons.Outlined.CalendarMonth,
                onClick = { onSelected(year) },
            )
        }

    }
}

@Composable
private fun RollTypeStep(uiState: ElectoralRollUiState, onSelected: (RollTypeDto) -> Unit) {
    ChoiceListScaffold(
        title = stringResource(R.string.ui_roll_title),
        subtitle = stringResource(R.string.ui_roll_help, uiState.selectedYear ?: 2026),
        headerIcon = Icons.AutoMirrored.Outlined.FactCheck,
        loading = uiState.isLoading,
        message = uiState.message,
    ) { _ ->
        items(uiState.rollTypes, key = { it.id }) { rollType ->
            ChoiceCard(
                title = rollType.displayName,
                subtitle = when {
                    rollType.displayName.contains("draft", ignoreCase = true) -> stringResource(R.string.ui_draft_help)
                    rollType.displayName.contains("final", ignoreCase = true) -> stringResource(R.string.ui_final_help)
                    rollType.displayName.contains("supplement", ignoreCase = true) -> stringResource(R.string.ui_supplement_help)
                    else -> null
                },
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                onClick = { onSelected(rollType) },
            )
        }

    }
}

@Composable
private fun DistrictStep(uiState: ElectoralRollUiState, onChangeSelection: () -> Unit, onSelected: (DistrictDto) -> Unit) {
    SearchableChoiceScreen(
        title = stringResource(R.string.ui_district_title),
        subtitle = stringResource(R.string.ui_district_help),
        queryPlaceholder = stringResource(R.string.ui_search_district),
        items = uiState.districts,
        itemTitle = { it.primaryDistrictName() },
        itemSubtitle = { it.hindiSubtitle(it.primaryDistrictName()) },
        headerIcon = Icons.Outlined.LocationOn,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        selectedSummary = uiState.selectedSummary(),
        onChangeSelection = onChangeSelection,
        onSelected = onSelected,
    )
}

@Composable
private fun AssemblyStep(uiState: ElectoralRollUiState, onSelected: (AssemblyDto) -> Unit, onChangeSelection: () -> Unit) {
    SearchableChoiceScreen(
        title = stringResource(R.string.ui_assembly_title),
        subtitle = stringResource(R.string.ui_assembly_help),
        queryPlaceholder = stringResource(R.string.ui_search_assembly),
        items = uiState.assemblies,
        itemTitle = { it.asmblyName },
        itemSubtitle = { it.asmblyName.hindiLineExcept(it.asmblyName) },
        headerIcon = Icons.Outlined.AccountBalance,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.assemblies.isEmpty(),
        message = uiState.message,
        selectedSummary = uiState.selectedSummary(),
        onChangeSelection = onChangeSelection,
        onSelected = onSelected,
    )
}

@Composable
internal fun PartsStep(
    uiState: ElectoralRollUiState,
    onPartToggled: (Int) -> Unit,
    onProceed: () -> Unit,
    onChangeSelection: () -> Unit,
) {
    var query by remember(uiState.selectedAssembly?.asmblyNo) { mutableStateOf("") }
    val filtered = remember(uiState.parts, query) {
        val term = query.trim()
        if (term.isBlank()) uiState.parts else uiState.parts.filter {
            it.partName.matchesSearchQuery(term) || it.partNumber.toString().matchesSearchQuery(term)
        }
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        Box(Modifier.weight(1f)) {
            ChoiceListScaffold(
                title = stringResource(R.string.ui_parts_title),
                subtitle = stringResource(R.string.ui_parts_help),
                headerIcon = Icons.Outlined.AccountBalance,
                loading = uiState.isLoading && uiState.parts.isEmpty(),
                message = uiState.message,
                selectedSummary = uiState.selectedSummary(),
                onChangeSelection = onChangeSelection,
                trailingHeader = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotBlank()) {{
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.ui_clear_search))
                            }
                        }} else null,
                        placeholder = { Text(stringResource(R.string.ui_search_parts)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = searchFieldColors(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    )
                    if (filtered.isEmpty() && !uiState.isLoading && uiState.parts.isNotEmpty()) {
                        Text(stringResource(R.string.ui_no_results), style = MaterialTheme.typography.bodyMedium)
                    }
                },
            ) { _ ->
                items(filtered, key = { it.partNumber }) { part ->
                    PartChoiceCard(part, part.partNumber in uiState.selectedPartNumbers) { onPartToggled(part.partNumber) }
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(stringResource(if (uiState.selectedPartNumbers.isEmpty()) R.string.ui_select_first else R.string.ui_selected_count, uiState.selectedPartNumbers.size),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                Button(
                    onClick = onProceed,
                    enabled = uiState.selectedPartNumbers.isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    contentPadding = PaddingValues(14.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.ui_proceed, uiState.selectedPartNumbers.size), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CaptchaStep(
    uiState: ElectoralRollUiState,
    onCaptchaChanged: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onStartDownload: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SummaryCard(uiState.selectedSummary()) }
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ui_captcha_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CaptchaImage(uiState.captcha?.captcha)
                        OutlinedButton(
                            onClick = onRefreshCaptcha,
                            enabled = !uiState.isCaptchaLoading && !uiState.isDownloading,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.ui_new_code))
                        }
                    }
                    OutlinedTextField(
                        value = uiState.captchaInput,
                        onValueChange = onCaptchaChanged,
                        enabled = !uiState.isDownloading,
                        label = { Text(stringResource(R.string.ui_captcha_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = onStartDownload,
                        enabled = uiState.captchaInput.isNotBlank() && uiState.captcha != null && !uiState.isCaptchaLoading && !uiState.isDownloading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(if (uiState.isDownloading) R.string.ui_downloading else R.string.ui_download_count, uiState.selectedPartNumbers.size))
                    }
                    uiState.message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.hasFailedDownloads) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = uiState.downloadItems.isNotEmpty() || uiState.isDownloading) {
                DownloadPanel(
                    uiState = uiState,
                    onRetry = onRetry,
                    onCancel = onCancel,
                )
            }
        }
    }
}

@Composable
private fun SuccessStep(uiState: ElectoralRollUiState, onOpenDownloads: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.ui_success_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.ui_success_count, uiState.completedCount, uiState.failedCount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Button(
                onClick = onOpenDownloads,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ui_saved_pdfs))
            }
        }
        items(uiState.downloadedPdfs, key = { it.uri }) { pdf ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = pdf.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageBottomSheet(
    uiState: ElectoralRollUiState,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ui_language_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            uiState.languages.entries.forEach { language ->
                ChoiceCard(
                    title = language.value,
                    subtitle = language.key,
                    leading = { Icon(Icons.Outlined.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { onSelected(language.key) },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun <T> SearchableChoiceScreen(
    title: String,
    subtitle: String,
    queryPlaceholder: String,
    showSearch: Boolean = true,
    items: List<T>,
    itemTitle: (T) -> String,
    itemSubtitle: (T) -> String?,
    loading: Boolean,
    message: String?,
    selectedSummary: String? = null,
    onChangeSelection: (() -> Unit)? = null,
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    onSelected: (T) -> Unit,
) {
    var query by remember(title) { mutableStateOf("") }
    val filtered = remember(items, query) {
        val term = query.trim()
        if (term.isBlank()) items else items.filter {
            itemTitle(it).matchesSearchQuery(term) ||
                itemSubtitle(it).orEmpty().matchesSearchQuery(term)
        }
    }

    ChoiceListScaffold(
        title = title,
        subtitle = subtitle,
        headerIcon = headerIcon,
        loading = loading,
        message = message,
        selectedSummary = selectedSummary,
        onChangeSelection = onChangeSelection,
        trailingHeader = if (showSearch) {
            {
                OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (query.isNotBlank()) {{
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.ui_clear_search))
                    }
                }} else null,
                placeholder = { Text(queryPlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = searchFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                )
            }
        } else null,
    ) { _ ->
        if (filtered.isEmpty() && !loading && items.isNotEmpty()) {
            item { Text(stringResource(R.string.ui_no_results), modifier = Modifier.padding(vertical = 16.dp)) }
        }
        filtered.forEachIndexed { index, item ->
            val titleKey = itemTitle(item)
            item(key = "choice-$title-$index-$titleKey") {
                ChoiceCard(
                    title = titleKey,
                    subtitle = itemSubtitle(item),
                    icon = itemIcon,
                    onClick = { onSelected(item) },
                )
            }
        }
    }
}

@Composable
private fun ChoiceListScaffold(
    title: String,
    subtitle: String,
    headerIcon: ImageVector,
    loading: Boolean,
    message: String?,
    selectedSummary: String? = null,
    onChangeSelection: (() -> Unit)? = null,
    trailingHeader: (@Composable ColumnScope.() -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.(LazyListState) -> Unit,
) {
    val listState = rememberLazyListState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepHeading(headerIcon, title, subtitle)
            SummaryCard(selectedSummary, onChangeSelection)
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            message?.takeUnless { loading }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            trailingHeader?.invoke(this)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content(listState) }
    }
}

@Composable
private fun StepHeading(icon: ImageVector, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun String.matchesSearchQuery(query: String): Boolean {
    val target = normalizedForSearch()
    val needle = query.normalizedForSearch()
    return needle.isBlank() ||
        target.contains(needle) ||
        target.replace(" ", "").contains(needle.replace(" ", ""))
}

private fun String.normalizedForSearch(): String = lowercase()
    .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()
@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.AccountBalance,
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (leading != null) leading() else Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PartChoiceCard(part: PartDto, selected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .heightIn(min = 80.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(part.partName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.ui_part_number, part.partNumber), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = selected, onCheckedChange = null)
        }
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
            .size(width = 160.dp, height = 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.ui_captcha_label),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DownloadPanel(
    uiState: ElectoralRollUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.ui_download_progress),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            LinearProgressIndicator(
                progress = { uiState.downloadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.ui_progress, uiState.downloadProgress, uiState.completedCount, uiState.downloadItems.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (uiState.failedCount > 0) Text(
                    text = stringResource(R.string.ui_failed_count, uiState.failedCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (uiState.failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedVisibility(visible = uiState.hasFailedDownloads && !uiState.isDownloading) {
                    OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(8.dp)) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_retry))
                    }
                }
                AnimatedVisibility(visible = uiState.isDownloading) {
                    OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp)) {
                        Icon(
                            Icons.Outlined.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_cancel))
                    }
                }
            }
            HorizontalDivider()
            uiState.downloadItems.forEach { item ->
                DownloadItemRow(item)
            }
        }
    }
}

@Composable
private fun DownloadItemRow(item: ElectoralRollDownloadItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = when (item.status) {
                DownloadStatus.Waiting -> Icons.Outlined.HourglassEmpty
                DownloadStatus.Downloading -> Icons.Outlined.FileDownload
                DownloadStatus.Completed -> Icons.Outlined.CheckCircle
                DownloadStatus.Failed -> Icons.Outlined.ErrorOutline
                DownloadStatus.Cancelled -> Icons.Outlined.Close
            },
            contentDescription = null,
            tint = when (item.status) {
                DownloadStatus.Completed -> MaterialTheme.colorScheme.primary
                DownloadStatus.Failed, DownloadStatus.Cancelled -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ui_part_number, item.partNumber),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.partName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.status.label(item.error),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (item.status == DownloadStatus.Downloading) {
                LinearProgressIndicator(
                    progress = { item.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: String?, onChange: (() -> Unit)? = null) {
    if (summary.isNullOrBlank()) return
    var expanded by rememberSaveable(summary) { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.ui_selected_details), style = MaterialTheme.typography.labelMedium)
                    if (!expanded) Text(summary.substringAfterLast(" / "), style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = stringResource(if (expanded) R.string.ui_hide_selection else R.string.ui_show_selection))
                }
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp)) {
                    Text(summary, style = MaterialTheme.typography.bodyMedium)
                    if (onChange != null) TextButton(onClick = onChange) { Text(stringResource(R.string.ui_change)) }
                }
            }
        }
    }
}

private fun ElectoralRollUiState.selectedSummary(): String? {
    val roll = selectedRollType?.displayName
    val year = selectedYear?.toString()
    val district = selectedDistrict?.displayName
    val assembly = selectedAssembly?.asmblyName
    return listOfNotNull(
        selectedState?.stateName,
        listOfNotNull(year, roll).joinToString(" · ").takeIf { it.isNotBlank() },
        district,
        assembly,
    ).takeIf { it.isNotEmpty() }?.joinToString(" / ")
}

@Composable
private fun DownloadStatus.label(error: String?): String =
    when (this) {
        DownloadStatus.Waiting -> stringResource(R.string.ui_status_waiting)
        DownloadStatus.Downloading -> stringResource(R.string.ui_downloading)
        DownloadStatus.Completed -> stringResource(R.string.ui_download_complete)
        DownloadStatus.Failed -> error ?: stringResource(R.string.ui_status_failed)
        DownloadStatus.Cancelled -> stringResource(R.string.ui_status_cancelled)
    }

private fun DistrictDto.primaryDistrictName(): String =
    listOfNotNull(districtValue, districtName, districtCd)
        .firstOrNull { !it.hasDevanagari() }
        ?: displayName

private fun DistrictDto.hindiSubtitle(primary: String): String? =
    listOfNotNull(districtName, districtValue)
        .firstOrNull { it.hasDevanagari() && it != primary }

private fun String.hindiLineExcept(primary: String): String? =
    takeIf { it.hasDevanagari() && it != primary }

private fun String.hasDevanagari(): Boolean =
    any { it in '\u0900'..'\u097F' }
