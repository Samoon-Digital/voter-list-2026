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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.core.ui.components.AdMobBannerAd
import com.samoondigital.yojnaplus.core.ui.components.AdMobNativeAd
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

private val WizardPurple = Color(0xFF3522A8)
private val WizardPurpleDark = Color(0xFF20106F)
private val WizardPurpleBright = Color(0xFF7C5CFF)
private val WizardInk = Color(0xFF090B1F)
private val WizardMuted = Color(0xFF686A8D)
private val WizardSurface = Color(0xFFFCFCFF)
private val WizardStroke = Color(0xFFE3E2F5)
private val ChoiceAccents = listOf(
    Color(0xFF4A2CC3),
    Color(0xFF43A66E),
    Color(0xFFD66C2E),
    Color(0xFF477CCA),
    Color(0xFFC83D77),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PdfScreen(
    onBack: () -> Unit,
    onDownloadsComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectoralRollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var openedDownloads by remember { mutableStateOf(false) }

    fun handleBack() {
        if (!viewModel.goBack()) onBack()
    }

    BackHandler(onBack = ::handleBack)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WizardSurface),
        ) {
            AnimatedContent(
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
                    ElectoralRollStep.District -> DistrictStep(uiState) { district ->
                        activity?.let { AdManager.showInterstitial(it) { viewModel.selectDistrict(district) } }
                            ?: viewModel.selectDistrict(district)
                    }
                    ElectoralRollStep.Assembly -> AssemblyStep(uiState, viewModel::selectAssembly)
                    ElectoralRollStep.Parts -> PartsStep(
                        uiState = uiState,
                        onPartToggled = viewModel::togglePart,
                        onProceed = viewModel::showLanguageSheet,
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
            onSelected = viewModel::selectLanguage,
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
            .height(140.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(WizardPurpleDark, WizardPurple, Color(0xFF2E1B98)),
                    start = Offset.Zero,
                    end = Offset(950f, 360f),
                ),
            ),
    ) {
        HeaderArtwork(modifier = Modifier.matchParentSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 18.dp, top = 10.dp, end = 18.dp),
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
                        contentDescription = "Back",
                        tint = WizardPurpleDark,
                        modifier = Modifier.size(24.dp),
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
                    text = "Voter List Download",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Step ${uiState.stepNumber} of 7",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
                WizardStepProgress(
                    currentStep = uiState.stepNumber,
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .fillMaxWidth(0.86f),
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
                        contentDescription = "Downloaded files",
                        tint = WizardPurpleDark,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val wave = Path().apply {
            moveTo(0f, h * 0.64f)
            cubicTo(w * 0.10f, h * 0.46f, w * 0.15f, h * 0.78f, w * 0.28f, h * 0.58f)
            cubicTo(w * 0.42f, h * 0.36f, w * 0.50f, h * 0.78f, w * 0.66f, h * 0.62f)
            cubicTo(w * 0.78f, h * 0.50f, w * 0.88f, h * 0.80f, w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wave, Color(0xFF5B49D7).copy(alpha = 0.30f))

        val map = Path().apply {
            moveTo(w * 0.68f, h * 0.20f)
            cubicTo(w * 0.72f, h * 0.12f, w * 0.76f, h * 0.18f, w * 0.76f, h * 0.26f)
            cubicTo(w * 0.83f, h * 0.25f, w * 0.89f, h * 0.35f, w * 0.86f, h * 0.44f)
            cubicTo(w * 0.91f, h * 0.50f, w * 0.84f, h * 0.55f, w * 0.78f, h * 0.51f)
            cubicTo(w * 0.74f, h * 0.58f, w * 0.66f, h * 0.53f, w * 0.70f, h * 0.45f)
            cubicTo(w * 0.63f, h * 0.39f, w * 0.67f, h * 0.30f, w * 0.68f, h * 0.20f)
            close()
        }
        drawPath(map, Color.White.copy(alpha = 0.13f))

        val dotColor = Color.White.copy(alpha = 0.18f)
        repeat(4) { row ->
            repeat(4) { col ->
                drawCircle(
                    color = dotColor,
                    radius = 4.3f,
                    center = Offset(w * 0.90f + col * 22f, h * 0.60f + row * 22f),
                )
            }
        }
    }
}

@Composable
private fun WizardStepProgress(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 7,
) {
    Row(
        modifier = modifier.height(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val completed = step <= currentStep
            Surface(
                shape = CircleShape,
                color = if (completed) Color.White else Color.Transparent,
                border = if (completed) null else BorderStroke(2.dp, Color.White),
                modifier = Modifier.size(if (step == currentStep) 13.dp else 11.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (completed) {
                        if (step == currentStep) {
                            Surface(
                                shape = CircleShape,
                                color = WizardPurpleBright,
                                modifier = Modifier.size(10.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(7.dp),
                                    )
                                }
                            }
                        } else {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = WizardPurpleDark,
                                modifier = Modifier.size(7.dp),
                            )
                        }
                    }
                }
            }
            if (index != totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(if (step < currentStep) Color.White else Color.White.copy(alpha = 0.55f)),
                )
            }
        }
    }
}

@Composable
private fun StateStep(uiState: ElectoralRollUiState, onSelected: (StateDto) -> Unit) {
    SearchableChoiceScreen(
        title = "Select State",
        subtitle = "Choose a state to load available electoral roll years.",
        queryPlaceholder = "Search state",
        items = uiState.states,
        itemTitle = StateDto::stateName,
        itemSubtitle = { null },
        headerIcon = Icons.Outlined.Map,
        itemIcon = Icons.Outlined.Map,
        loading = uiState.isLoading && uiState.states.isEmpty(),
        message = uiState.message,
        showSearch = false,
        headerAd = { AdMobBannerAd() },
        showInlineNativeAds = true,
        onSelected = onSelected,
    )
}

@Composable
private fun YearStep(uiState: ElectoralRollUiState, onSelected: (Int) -> Unit) {
    ChoiceListScaffold(
        title = "Select Year",
        subtitle = "Years are loaded from available ECI roll types for ${uiState.selectedState?.stateName.orEmpty()}.",
        headerIcon = Icons.Outlined.CalendarMonth,
        loading = uiState.isLoading,
        message = uiState.message,
    ) {
        itemsIndexed(uiState.years, key = { _, year -> year }) { index, year ->
            ChoiceCard(
                title = year.toString(),
                subtitle = null,
                icon = Icons.Outlined.CalendarMonth,
                accentIndex = index,
                onClick = { onSelected(year) },
            )
        }
        if (uiState.years.isNotEmpty()) {
            item { AdMobNativeAd() }
        }
    }
}

@Composable
private fun RollTypeStep(uiState: ElectoralRollUiState, onSelected: (RollTypeDto) -> Unit) {
    ChoiceListScaffold(
        title = "Select Roll Type",
        subtitle = "Loaded dynamically for ${uiState.selectedYear ?: ""}.",
        headerIcon = Icons.Outlined.FactCheck,
        loading = uiState.isLoading,
        message = uiState.message,
    ) {
        itemsIndexed(uiState.rollTypes, key = { _, rollType -> rollType.id }) { index, rollType ->
            ChoiceCard(
                title = rollType.displayName,
                subtitle = null,
                icon = Icons.Outlined.FactCheck,
                accentIndex = index,
                onClick = { onSelected(rollType) },
            )
        }
    }
}

@Composable
private fun DistrictStep(uiState: ElectoralRollUiState, onSelected: (DistrictDto) -> Unit) {
    SearchableChoiceScreen(
        title = "Select District",
        subtitle = "Choose district to load assembly constituencies.",
        queryPlaceholder = "Search district",
        items = uiState.districts,
        itemTitle = { it.primaryDistrictName() },
        itemSubtitle = { it.hindiSubtitle(it.primaryDistrictName()) },
        headerIcon = Icons.Outlined.LocationOn,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        selectedSummary = uiState.selectedSummary(),
        headerAd = { AdMobBannerAd() },
        showInlineNativeAds = true,
        onSelected = onSelected,
    )
}

@Composable
private fun AssemblyStep(uiState: ElectoralRollUiState, onSelected: (AssemblyDto) -> Unit) {
    SearchableChoiceScreen(
        title = "Select Assembly Constituency",
        subtitle = "Choose your assembly constituency.",
        queryPlaceholder = "Search assembly",
        items = uiState.assemblies,
        itemTitle = { it.asmblyName },
        itemSubtitle = { it.asmblyName.hindiLineExcept(it.asmblyName) },
        headerIcon = Icons.Outlined.AccountBalance,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.assemblies.isEmpty(),
        message = uiState.message,
        selectedSummary = uiState.selectedSummary(),
        showInlineNativeAds = true,
        onSelected = onSelected,
    )
}

@Composable
private fun PartsStep(
    uiState: ElectoralRollUiState,
    onPartToggled: (Int) -> Unit,
    onProceed: () -> Unit,
) {
    var query by remember(uiState.selectedAssembly?.asmblyNo) { mutableStateOf("") }
    val filtered = remember(uiState.parts, query) {
        val term = query.trim()
        if (term.isBlank()) uiState.parts else uiState.parts.filter {
            it.partName.contains(term, ignoreCase = true) ||
                it.partNumber.toString().contains(term)
        }
    }

    Box(Modifier.fillMaxSize()) {
        ChoiceListScaffold(
            title = "Select Village / Part List",
            subtitle = "Select up to 10 polling parts. Proceed appears after selection.",
            headerIcon = Icons.Outlined.AccountBalance,
            loading = uiState.isLoading && uiState.parts.isEmpty(),
            message = uiState.message,
            selectedSummary = uiState.selectedSummary(),
            headerAd = { AdMobBannerAd() },
            trailingHeader = {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Outlined.Tune, contentDescription = null, tint = WizardPurple) },
                    placeholder = { Text("Search part or village") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = searchFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${uiState.selectedPartNumbers.size}/10 selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            itemsIndexed(filtered, key = { _, part -> part.partNumber }) { index, part ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (index > 0 && index % 7 == 0) AdMobNativeAd()
                    PartChoiceCard(
                        part = part,
                        selected = part.partNumber in uiState.selectedPartNumbers,
                        onClick = { onPartToggled(part.partNumber) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedPartNumbers.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
                .navigationBarsPadding(),
        ) {
            ExtendedFloatingActionButton(
                onClick = onProceed,
                icon = { Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null) },
                text = { Text("Proceed (${uiState.selectedPartNumbers.size}/10)") },
            )
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
                        text = "Enter Captcha",
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
                            Text("Refresh")
                        }
                    }
                    OutlinedTextField(
                        value = uiState.captchaInput,
                        onValueChange = onCaptchaChanged,
                        enabled = !uiState.isDownloading,
                        label = { Text("Captcha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = onStartDownload,
                        enabled = uiState.captchaInput.isNotBlank() && !uiState.isDownloading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Start Download")
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
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                }
            }
        }
        item {
            Text(
                text = "PDFs Downloaded",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${uiState.completedCount} completed, ${uiState.failedCount} failed",
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
                    .height(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Downloads")
            }
        }
        items(uiState.downloadedPdfs, key = { it.uri }) { pdf ->
            ElevatedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = pdf.fileName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
                text = "Select Download Language",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            uiState.languages.entries.forEach { language ->
                ChoiceCard(
                    title = language.value,
                    subtitle = language.key,
                    leading = { Icon(Icons.Outlined.Language, contentDescription = null) },
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
    headerAd: (@Composable ColumnScope.() -> Unit)? = null,
    showInlineNativeAds: Boolean = false,
    inlineNativeEvery: Int = 6,
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    onSelected: (T) -> Unit,
) {
    var query by remember(title) { mutableStateOf("") }
    val filtered = remember(items, query) {
        val term = query.trim()
        if (term.isBlank()) items else items.filter {
            itemTitle(it).contains(term, ignoreCase = true) ||
                itemSubtitle(it).orEmpty().contains(term, ignoreCase = true)
        }
    }

    ChoiceListScaffold(
        title = title,
        subtitle = subtitle,
        headerIcon = headerIcon,
        loading = loading,
        message = message,
        selectedSummary = selectedSummary,
        headerAd = headerAd,
        trailingHeader = if (showSearch) {
            {
                OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Outlined.Tune, contentDescription = null, tint = WizardPurple) },
                placeholder = { Text(queryPlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = searchFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                )
            }
        } else null,
    ) {
        itemsIndexed(filtered) { index, item ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showInlineNativeAds && index > 0 && index % inlineNativeEvery == 0) AdMobNativeAd()
                ChoiceCard(
                    title = itemTitle(item),
                    subtitle = itemSubtitle(item),
                    icon = itemIcon,
                    accentIndex = index,
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
    headerAd: (@Composable ColumnScope.() -> Unit)? = null,
    trailingHeader: (@Composable ColumnScope.() -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StepHeading(
                    icon = headerIcon,
                    title = title,
                    subtitle = subtitle,
                )
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
                headerAd?.invoke(this)
                trailingHeader?.invoke(this)
            }
        }
        content()
        item { Spacer(Modifier.height(88.dp)) }
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
            color = Color(0xFFF0EEFF),
            modifier = Modifier.size(50.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = WizardPurple,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WizardInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WizardMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WizardStroke,
    unfocusedBorderColor = WizardStroke,
    focusedLeadingIconColor = WizardMuted,
    unfocusedLeadingIconColor = WizardMuted,
    focusedPlaceholderColor = WizardMuted,
    unfocusedPlaceholderColor = WizardMuted,
    cursorColor = WizardPurple,
    focusedTextColor = WizardInk,
    unfocusedTextColor = WizardInk,
)

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.AccountBalance,
    accentIndex: Int = 0,
) {
    val accent = ChoiceAccents[accentIndex % ChoiceAccents.size]
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color(0xFFE9E8F8),
                spotColor = Color(0xFFE9E8F8),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leading != null) {
                leading()
            } else {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(50.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = WizardInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WizardInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = WizardPurple,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PartChoiceCard(part: PartDto, selected: Boolean, onClick: () -> Unit) {
    val accent = ChoiceAccents[(part.partNumber - 1).floorMod(ChoiceAccents.size)]
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) Color(0xFFF2EFFF) else Color.White,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color(0xFFE9E8F8),
                spotColor = Color(0xFFE9E8F8),
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.12f),
                modifier = Modifier.size(50.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Part ${part.partNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = WizardInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = part.partName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WizardInk,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = CircleShape,
                color = if (selected) WizardPurple else Color(0xFFE9E8F8),
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (selected) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
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
                contentDescription = "Captcha",
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
                text = "Download Progress",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            LinearProgressIndicator(
                progress = { uiState.downloadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "${uiState.completedCount} of ${uiState.downloadItems.size} completed",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Failed: ${uiState.failedCount}",
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
                        Text("Retry")
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
                        Text("Cancel")
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
private fun SummaryCard(summary: String?) {
    if (summary.isNullOrBlank()) return
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(14.dp),
        )
    }
}

private fun ElectoralRollUiState.toolbarTitle(): String =
    when (step) {
        ElectoralRollStep.State -> "Electoral Roll"
        ElectoralRollStep.Year -> selectedState?.stateName ?: "Select Year"
        ElectoralRollStep.RollType -> selectedState?.stateName ?: "Roll Type"
        ElectoralRollStep.District -> selectedState?.stateName ?: "District"
        ElectoralRollStep.Assembly -> selectedState?.stateName ?: "Assembly"
        ElectoralRollStep.Parts -> selectedState?.stateName ?: "Parts"
        ElectoralRollStep.Captcha -> "Enter Captcha"
        ElectoralRollStep.Success -> "Download Complete"
    }

private fun ElectoralRollUiState.selectedSummary(): String? {
    val roll = selectedRollType?.displayName
    val year = selectedYear?.toString()
    val district = selectedDistrict?.displayName
    val assembly = selectedAssembly?.asmblyName
    return listOfNotNull(
        if (roll != null && year != null) "$roll - $year" else null,
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

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
