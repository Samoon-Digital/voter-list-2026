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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import com.samoondigital.yojnaplus.core.ui.theme.IndigoDark
import com.samoondigital.yojnaplus.core.ui.theme.Indigo
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


private val WizardMuted = Color(0xFF686A8D)
private val WizardStroke = Color(0xFFE3E2F5)
private const val GeneralDistrictPlacement = "general-voter-list-district-to-assembly"
private const val GeneralLanguagePlacement = "general-voter-list-language-to-captcha"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PdfScreen(
    onBack: () -> Unit,
    onDownloadsComplete: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBannerHeight: Dp = 0.dp,
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
            WizardHeroTopBar(
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
                .padding(bottom = bottomBannerHeight)
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
private fun WizardHeroTopBar(
    uiState: ElectoralRollUiState,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(IndigoDark, Indigo),
                    start = Offset.Zero,
                    end = Offset(950f, 360f),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding()
                .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.ui_back),
                        tint = IndigoDark,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.ui_wizard_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.ui_step, uiState.stepNumber),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
            }

            Surface(
                onClick = onOpenDownloads,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = stringResource(R.string.ui_saved_pdfs),
                        tint = IndigoDark,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StateStep(uiState: ElectoralRollUiState, onSelected: (StateDto) -> Unit) {
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
    Column(Modifier.fillMaxSize()) {
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
                    Text(stringResource(R.string.ui_selected_count, uiState.selectedPartNumbers.size), style = MaterialTheme.typography.labelLarge)
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
        Surface(shadowElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (uiState.selectedPartNumbers.isEmpty()) {
                    Text(stringResource(R.string.ui_select_first), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                }
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

@Composable
private fun CaptchaStep(
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
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SummaryCard(uiState.selectedSummary()) }
        item {
            ElevatedCard(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ui_captcha_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                HorizontalDivider(color = WizardStroke)
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
    ) { listState ->
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
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "choice-header-$title") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StepHeading(
                    icon = headerIcon,
                    title = title,
                    subtitle = subtitle,
                )
                SummaryCard(selectedSummary, onChangeSelection)
                if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                message?.takeUnless { loading }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                trailingHeader?.invoke(this)
            }
        }
        content(listState)
        item(key = "choice-bottom-space-$title") { Spacer(Modifier.height(88.dp)) }
    }
}
@Composable
private fun StepHeading(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(50.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
    focusedBorderColor = WizardStroke,
    unfocusedBorderColor = WizardStroke,
    focusedLeadingIconColor = WizardMuted,
    unfocusedLeadingIconColor = WizardMuted,
    focusedPlaceholderColor = WizardMuted,
    unfocusedPlaceholderColor = WizardMuted,
    cursorColor = Indigo,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.AccountBalance,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 2.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leading != null) {
                leading()
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        HorizontalDivider(color = WizardStroke)
    }
}

@Composable
private fun PartChoiceCard(part: PartDto, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() }),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Checkbox(checked = selected, onCheckedChange = null)
            Column(Modifier.weight(1f)) {
                Text(part.partName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.ui_part_number, part.partNumber), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
    ElevatedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.ui_progress, uiState.downloadProgress, uiState.completedCount, uiState.downloadItems.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
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
                text = "Part ${item.partNumber}",
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.ui_selected_details), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                if (onChange != null) TextButton(onClick = onChange) { Text(stringResource(R.string.ui_change)) }
            }
            Text(summary, style = MaterialTheme.typography.bodyMedium)
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

private fun DownloadStatus.label(error: String?): String =
    when (this) {
        DownloadStatus.Waiting -> "Waiting in queue"
        DownloadStatus.Downloading -> "Downloading..."
        DownloadStatus.Completed -> "Completed"
        DownloadStatus.Failed -> error ?: "Failed"
        DownloadStatus.Cancelled -> "Cancelled"
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
