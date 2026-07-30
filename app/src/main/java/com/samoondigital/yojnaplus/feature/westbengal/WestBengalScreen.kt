package com.samoondigital.yojnaplus.feature.westbengal

import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val WbPurple = Color(0xFF3522A8)
private val WbPurpleDark = Color(0xFF20106F)
private val WbPurpleBright = Color(0xFF7C5CFF)
private val WbInk = Color(0xFF090B1F)
private val WbMuted = Color(0xFF686A8D)
private val WbSurface = Color(0xFFFCFCFF)
private val WbStroke = Color(0xFFE3E2F5)
private val WbAccents = listOf(
    Color(0xFF4A2CC3),
    Color(0xFF43A66E),
    Color(0xFFD66C2E),
    Color(0xFF477CCA),
    Color(0xFFC83D77),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WestBengalScreen(
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WestBengalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is WestBengalEvent.OpenPdf -> onOpenPdf(event.uri, event.title)
            }
        }
    }

    fun handleBack() {
        if (!viewModel.goBack()) onBack()
    }

    BackHandler(onBack = ::handleBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WestBengalTopBar(
                uiState = uiState,
                onBack = ::handleBack,
                onOpenDownloads = onOpenDownloads,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WbSurface),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "west-bengal-step",
            ) { step ->
                when (step) {
                    WestBengalStep.District -> DistrictStep(uiState, viewModel::selectDistrict)
                    WestBengalStep.Assembly -> AssemblyStep(uiState, viewModel::selectAssembly)
                    WestBengalStep.PollingStation -> PollingStationStep(uiState, viewModel::selectPart)
                    WestBengalStep.Captcha -> CaptchaStep(
                        uiState = uiState,
                        onCaptchaChanged = viewModel::updateCaptchaInput,
                        onRefreshCaptcha = viewModel::refreshCaptcha,
                        onDownload = viewModel::confirmCaptcha,
                    )
                }
            }
        }
    }
}

@Composable
private fun WestBengalTopBar(
    uiState: WestBengalUiState,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(WbPurpleDark, WbPurple, Color(0xFF2E1B98)),
                    start = Offset.Zero,
                    end = Offset(950f, 360f),
                ),
            ),
    ) {
        HeaderArtwork(modifier = Modifier.matchParentSize())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding()
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
                        tint = WbPurpleDark,
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
                    text = "West Bengal 2002",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Step ${uiState.stepNumber} of 4",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
                StepProgress(
                    currentStep = uiState.stepNumber,
                    totalSteps = 4,
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
                        contentDescription = "Open downloads",
                        tint = WbPurpleDark,
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
    }
}

@Composable
private fun StepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
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
                                color = WbPurpleBright,
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
                                tint = WbPurpleDark,
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
private fun DistrictStep(uiState: WestBengalUiState, onSelected: (WestBengalDistrict) -> Unit) {
    ChoiceScreen(
        title = "Select District",
        subtitle = "Electoral Roll 2002",
        queryPlaceholder = "Search district",
        items = uiState.districts,
        itemTitle = WestBengalDistrict::name,
        itemSubtitle = { "District ${it.id}" },
        headerIcon = Icons.Outlined.Map,
        itemIcon = Icons.Outlined.LocationOn,
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
    )
}

@Composable
private fun AssemblyStep(uiState: WestBengalUiState, onSelected: (WestBengalAssembly) -> Unit) {
    ChoiceScreen(
        title = "Select Assembly",
        subtitle = uiState.selectedDistrict?.name ?: "Choose assembly",
        queryPlaceholder = "Search assembly",
        items = uiState.assemblies,
        itemTitle = WestBengalAssembly::displayName,
        itemSubtitle = { null },
        headerIcon = Icons.Outlined.AccountBalance,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.assemblies.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
    )
}

@Composable
private fun PollingStationStep(
    uiState: WestBengalUiState,
    onSelected: (WestBengalPart) -> Unit,
) {
    ChoiceScreen(
        title = "Select Polling Station",
        subtitle = uiState.selectedAssembly?.displayName ?: "Choose polling station",
        queryPlaceholder = "Search polling station",
        items = uiState.parts,
        itemTitle = WestBengalPart::displayName,
        itemSubtitle = { null },
        headerIcon = Icons.Outlined.PictureAsPdf,
        itemIcon = Icons.Outlined.PictureAsPdf,
        loading = uiState.isLoading && uiState.parts.isEmpty(),
        message = uiState.message,
        itemTitleMaxLines = Int.MAX_VALUE,
        onSelected = onSelected,
    )
}

@Composable
private fun CaptchaStep(
    uiState: WestBengalUiState,
    onCaptchaChanged: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onDownload: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "wb-captcha-header") {
            StepHeading(
                icon = Icons.Outlined.PictureAsPdf,
                title = "Enter Captcha",
                subtitle = uiState.selectedPart?.displayName ?: "Verify before download",
            )
        }
        item(key = "wb-captcha-progress") {
            if (uiState.isDownloading) {
                LinearProgressIndicator(
                    progress = { uiState.downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uiState.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.contains("success", ignoreCase = true)) WbPurple else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item(key = "wb-captcha-card") {
            ElevatedCard(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF4F2FF),
                            modifier = Modifier
                                .weight(1f)
                                .height(88.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.captchaCode,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = WbInk,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                )
                            }
                        }
                        IconButton(
                            onClick = onRefreshCaptcha,
                            enabled = !uiState.isDownloading,
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh captcha")
                        }
                    }
                    OutlinedTextField(
                        value = uiState.captchaInput,
                        onValueChange = onCaptchaChanged,
                        label = { Text("Captcha") },
                        singleLine = true,
                        colors = searchFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = onDownload,
                        enabled = uiState.captchaInput.length == 5 && !uiState.isDownloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        if (uiState.isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 3.dp,
                                color = Color.White,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("${uiState.downloadProgress}%")
                        } else {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download PDF")
                        }
                    }
                }
            }
        }
        item(key = "wb-captcha-space") { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun <T> ChoiceScreen(
    title: String,
    subtitle: String,
    queryPlaceholder: String,
    items: List<T>,
    itemTitle: (T) -> String,
    itemSubtitle: (T) -> String?,
    loading: Boolean,
    message: String?,
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    itemTitleMaxLines: Int = 1,
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "wb-header-$title") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StepHeading(headerIcon, title, subtitle)
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                message?.takeUnless { loading }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text(queryPlaceholder) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = searchFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                )
                HorizontalDivider(color = WbStroke)
            }
        }
        itemsIndexed(
            items = filtered,
            key = { index, item -> "wb-$title-$index-${itemTitle(item)}" },
        ) { index, item ->
            ChoiceCard(
                title = itemTitle(item),
                subtitle = itemSubtitle(item),
                icon = itemIcon,
                accentIndex = index,
                titleMaxLines = itemTitleMaxLines,
                onClick = { onSelected(item) },
            )
        }
        item(key = "wb-bottom-space-$title") { Spacer(Modifier.height(88.dp)) }
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
                    tint = WbPurple,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WbInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WbMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    icon: ImageVector,
    accentIndex: Int,
    titleMaxLines: Int = 1,
) {
    val accent = WbAccents[accentIndex % WbAccents.size]
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = WbInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WbInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = WbPurple,
                modifier = Modifier.size(24.dp),
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
    focusedBorderColor = WbStroke,
    unfocusedBorderColor = WbStroke,
    focusedLeadingIconColor = WbMuted,
    unfocusedLeadingIconColor = WbMuted,
    focusedPlaceholderColor = WbMuted,
    unfocusedPlaceholderColor = WbMuted,
    cursorColor = WbPurple,
    focusedTextColor = WbInk,
    unfocusedTextColor = WbInk,
)
