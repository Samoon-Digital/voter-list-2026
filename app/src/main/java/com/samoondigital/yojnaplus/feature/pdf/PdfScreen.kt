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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.ads.InlineBanner
import com.samoondigital.yojnaplus.ads.rememberInlineBanner
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
            SelectionToolbar(
                uiState = uiState,
                onBack = ::handleBack,
                onOpenDownloads = onDownloadsComplete,
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            val banner = rememberInlineBanner((maxWidth - 32.dp).value.toInt())
            val inlineBanner: @Composable () -> Unit = { InlineBanner(banner) }
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "electoral-roll-step",
            ) { step ->
                when (step) {
                    ElectoralRollStep.State -> StateStep(uiState, inlineBanner, viewModel::selectState)
                    ElectoralRollStep.Year -> YearStep(uiState, inlineBanner, viewModel::selectYear)
                    ElectoralRollStep.RollType -> RollTypeStep(uiState, inlineBanner, viewModel::selectRollType)
                    ElectoralRollStep.District -> DistrictStep(uiState, inlineBanner, viewModel::selectDistrict)
                    ElectoralRollStep.Assembly -> AssemblyStep(uiState, inlineBanner, viewModel::selectAssembly)
                    ElectoralRollStep.Parts -> PartsStep(
                        uiState = uiState,
                        onPartToggled = viewModel::togglePart,
                        onProceed = viewModel::showLanguageSheet,
                        inlineBanner = inlineBanner,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionToolbar(
    uiState: ElectoralRollUiState,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var panel by rememberSaveable(uiState.step) { mutableStateOf<String?>(null) }
    val summary = uiState.selectedSummary()
    AppToolbar(title = selectionTitle(uiState.step), onBack = onBack) {
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.ui_options))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (summary != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ui_show_selection)) },
                        onClick = { menuOpen = false; panel = "selection" },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_help)) },
                    onClick = { menuOpen = false; panel = "help" },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_downloads_title)) },
                    onClick = { menuOpen = false; onOpenDownloads() },
                )
            }
        }
    }
    if (panel != null) {
        ModalBottomSheet(
            onDismissRequest = { panel = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(if (panel == "help") R.string.ui_help else R.string.ui_selected_details),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    if (panel == "help") selectionHelp(uiState) else summary.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (panel == "selection") {
                    if (uiState.selectedPartNumbers.isNotEmpty()) {
                        Text(stringResource(R.string.ui_selected_count, uiState.selectedPartNumbers.size))
                    }
                    uiState.selectedLanguageCode?.let { code ->
                        Text(stringResource(R.string.ui_chosen_language, uiState.languages[code] ?: code))
                    }
                    if (uiState.step != ElectoralRollStep.State && uiState.step != ElectoralRollStep.Success && !uiState.isDownloading) {
                        TextButton(onClick = { panel = null; onBack() }) {
                            Text(stringResource(R.string.ui_change_previous))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun selectionTitle(step: ElectoralRollStep): String = stringResource(when (step) {
    ElectoralRollStep.State -> R.string.ui_state_title
    ElectoralRollStep.Year -> R.string.ui_year_title
    ElectoralRollStep.RollType -> R.string.ui_roll_title
    ElectoralRollStep.District -> R.string.ui_district_title
    ElectoralRollStep.Assembly -> R.string.ui_assembly_title
    ElectoralRollStep.Parts -> R.string.ui_parts_title
    ElectoralRollStep.Captcha -> R.string.ui_wizard_title
    ElectoralRollStep.Success -> R.string.ui_success_title
})

@Composable
private fun selectionHelp(state: ElectoralRollUiState): String = when (state.step) {
    ElectoralRollStep.State -> stringResource(R.string.ui_state_help)
    ElectoralRollStep.Year -> stringResource(R.string.ui_year_help, state.selectedState?.stateName.orEmpty())
    ElectoralRollStep.RollType -> stringResource(R.string.ui_roll_help, state.selectedYear ?: 2026)
    ElectoralRollStep.District -> stringResource(R.string.ui_district_help)
    ElectoralRollStep.Assembly -> stringResource(R.string.ui_assembly_help)
    ElectoralRollStep.Parts -> stringResource(R.string.ui_parts_help)
    ElectoralRollStep.Captcha -> stringResource(R.string.ui_captcha_help)
    ElectoralRollStep.Success -> stringResource(R.string.ui_success_count, state.completedCount, state.failedCount)
}

@Composable
internal fun StateStep(
    uiState: ElectoralRollUiState,
    inlineBanner: (@Composable () -> Unit)? = null,
    onSelected: (StateDto) -> Unit,
) {
    SearchableChoiceScreen(
        queryPlaceholder = stringResource(R.string.ui_search_state),
        items = uiState.states,
        itemKey = { it.stateCd },
        itemTitle = StateDto::stateName,
        itemSubtitle = { null },
        loading = uiState.isLoading && uiState.states.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
        inlineBanner = inlineBanner,
    )
}

@Composable
private fun YearStep(
    uiState: ElectoralRollUiState,
    inlineBanner: (@Composable () -> Unit)? = null,
    onSelected: (Int) -> Unit,
) {
    ChoiceListScaffold(loading = uiState.isLoading, message = uiState.message) {
        choicesWithInlineBanner(uiState.years, key = { it }, inlineBanner = inlineBanner) { year ->
            ChoiceCard(title = year.toString(), subtitle = null, onClick = { onSelected(year) })
        }
    }
}

@Composable
private fun RollTypeStep(
    uiState: ElectoralRollUiState,
    inlineBanner: (@Composable () -> Unit)? = null,
    onSelected: (RollTypeDto) -> Unit,
) {
    ChoiceListScaffold(loading = uiState.isLoading, message = uiState.message) {
        choicesWithInlineBanner(uiState.rollTypes, key = { it.id }, inlineBanner = inlineBanner) { rollType ->
            ChoiceCard(
                title = rollType.displayName,
                subtitle = when {
                    rollType.displayName.contains("draft", ignoreCase = true) -> stringResource(R.string.ui_draft_help)
                    rollType.displayName.contains("final", ignoreCase = true) -> stringResource(R.string.ui_final_help)
                    rollType.displayName.contains("supplement", ignoreCase = true) -> stringResource(R.string.ui_supplement_help)
                    else -> null
                },
                onClick = { onSelected(rollType) },
            )
        }
    }
}

@Composable
private fun DistrictStep(
    uiState: ElectoralRollUiState,
    inlineBanner: (@Composable () -> Unit)? = null,
    onSelected: (DistrictDto) -> Unit,
) {
    SearchableChoiceScreen(
        queryPlaceholder = stringResource(R.string.ui_search_district),
        items = uiState.districts,
        itemKey = { it.districtCd },
        itemTitle = { it.primaryDistrictName() },
        itemSubtitle = { it.hindiSubtitle(it.primaryDistrictName()) },
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
        inlineBanner = inlineBanner,
    )
}

@Composable
private fun AssemblyStep(
    uiState: ElectoralRollUiState,
    inlineBanner: (@Composable () -> Unit)? = null,
    onSelected: (AssemblyDto) -> Unit,
) {
    SearchableChoiceScreen(
        queryPlaceholder = stringResource(R.string.ui_search_assembly),
        items = uiState.assemblies,
        itemKey = { it.asmblyNo },
        itemTitle = { it.asmblyName },
        itemSubtitle = { it.asmblyName.hindiLineExcept(it.asmblyName) },
        loading = uiState.isLoading && uiState.assemblies.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
        inlineBanner = inlineBanner,
    )
}

@Composable
internal fun PartsStep(
    uiState: ElectoralRollUiState,
    onPartToggled: (Int) -> Unit,
    onProceed: () -> Unit,
    inlineBanner: (@Composable () -> Unit)? = null,
) {
    var query by rememberSaveable(uiState.selectedAssembly?.asmblyNo) { mutableStateOf("") }
    val filtered = remember(uiState.parts, query) {
        val term = query.trim()
        if (term.isBlank()) uiState.parts else uiState.parts.filter {
            it.partName.matchesSearchQuery(term) || it.partNumber.toString().matchesSearchQuery(term)
        }
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        Box(Modifier.weight(1f)) {
            ChoiceListScaffold(
                loading = uiState.isLoading && uiState.parts.isEmpty(),
                message = uiState.message,
                search = {
                    ChoiceSearchField(query, { query = it }, stringResource(R.string.ui_search_parts))
                },
            ) {
                if (filtered.isEmpty() && !uiState.isLoading && uiState.parts.isNotEmpty()) {
                    item { NoResults() }
                }
                choicesWithInlineBanner(filtered, key = { it.partNumber }, inlineBanner = inlineBanner) { part ->
                    PartChoiceCard(part, part.partNumber in uiState.selectedPartNumbers) {
                        onPartToggled(part.partNumber)
                    }
                }
            }
        }
        val actionDescription = stringResource(R.string.ui_proceed, uiState.selectedPartNumbers.size)
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.ui_selected_short, uiState.selectedPartNumbers.size),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onProceed,
                    enabled = uiState.selectedPartNumbers.isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = actionDescription },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(stringResource(R.string.ui_continue))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        shape = RoundedCornerShape(12.dp),
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
                    onClick = { onSelected(language.key) },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun <T> SearchableChoiceScreen(
    queryPlaceholder: String,
    items: List<T>,
    itemKey: (T) -> Any,
    itemTitle: (T) -> String,
    itemSubtitle: (T) -> String?,
    loading: Boolean,
    message: String?,
    onSelected: (T) -> Unit,
    inlineBanner: (@Composable () -> Unit)? = null,
) {
    var query by rememberSaveable(queryPlaceholder) { mutableStateOf("") }
    val filtered = remember(items, query) {
        val term = query.trim()
        if (term.isBlank()) items else items.filter {
            itemTitle(it).matchesSearchQuery(term) || itemSubtitle(it).orEmpty().matchesSearchQuery(term)
        }
    }
    ChoiceListScaffold(
        loading = loading,
        message = message,
        search = { ChoiceSearchField(query, { query = it }, queryPlaceholder) },
    ) {
        if (filtered.isEmpty() && !loading && items.isNotEmpty()) item { NoResults() }
        choicesWithInlineBanner(filtered, key = itemKey, inlineBanner = inlineBanner) { item ->
            ChoiceCard(itemTitle(item), itemSubtitle(item), onClick = { onSelected(item) })
        }
    }
}

internal fun <T> androidx.compose.foundation.lazy.LazyListScope.choicesWithInlineBanner(
    choices: List<T>,
    key: (T) -> Any,
    inlineBanner: (@Composable () -> Unit)?,
    itemContent: @Composable (T) -> Unit,
) {
    val split = minOf(5, choices.size)
    items(count = split, key = { key(choices[it]) }) { itemContent(choices[it]) }
    if (choices.isNotEmpty() && inlineBanner != null) {
        item(key = "inline-banner", contentType = "inline-banner") { inlineBanner() }
    }
    items(count = choices.size - split, key = { key(choices[it + split]) }) {
        itemContent(choices[it + split])
    }
}

@Composable
private fun ChoiceSearchField(query: String, onQueryChanged: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = if (query.isNotBlank()) {{
            IconButton(onClick = { onQueryChanged("") }) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.ui_clear_search))
            }
        }} else null,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = searchFieldColors(),
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChoiceListScaffold(
    loading: Boolean,
    message: String?,
    search: (@Composable () -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (search != null) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) { search() }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("selection-list"),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            message?.takeUnless { loading }?.let { message ->
                item {
                    Text(message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error)
                }
            }
            content()
        }
    }
}

@Composable
private fun NoResults() {
    Text(stringResource(R.string.ui_no_results), Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun ChoiceCard(title: String, subtitle: String?, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun PartChoiceCard(part: PartDto, selected: Boolean, onClick: () -> Unit) {
    Surface(color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
                .heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(part.partName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.ui_part_number, part.partNumber), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = selected, onCheckedChange = null)
        }
    }
    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
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
