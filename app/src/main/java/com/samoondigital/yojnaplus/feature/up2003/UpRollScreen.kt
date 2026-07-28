package com.samoondigital.yojnaplus.feature.up2003

import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Path
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val UpPurple = Color(0xFF3522A8)
private val UpPurpleDark = Color(0xFF20106F)
private val UpPurpleBright = Color(0xFF7C5CFF)
private val UpInk = Color(0xFF090B1F)
private val UpMuted = Color(0xFF686A8D)
private val UpSurface = Color(0xFFFCFCFF)
private val UpStroke = Color(0xFFE3E2F5)
private val Accents = listOf(
    Color(0xFF4A2CC3),
    Color(0xFF43A66E),
    Color(0xFFD66C2E),
    Color(0xFF477CCA),
    Color(0xFFC83D77),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UpRollScreen(
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpRollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UpRollEvent.OpenPdf -> onOpenPdf(event.uri, event.title)
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
            UpTopBar(
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
                .background(UpSurface),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "up-roll-step",
            ) { step ->
                when (step) {
                    UpRollStep.District -> DistrictStep(uiState, viewModel::selectDistrict)
                    UpRollStep.Assembly -> AssemblyStep(uiState, viewModel::selectAssembly)
                    UpRollStep.PollingStation -> PollingStationStep(uiState, viewModel::requestDownload)
                }
            }
        }
    }
}

@Composable
private fun UpTopBar(
    uiState: UpRollUiState,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(UpPurpleDark, UpPurple, Color(0xFF2E1B98)),
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
                        tint = UpPurpleDark,
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
                    text = "Uttar Pradesh 2003",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Step ${uiState.stepNumber} of 3",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
                StepProgress(
                    currentStep = uiState.stepNumber,
                    totalSteps = 3,
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
                        tint = UpPurpleDark,
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
                                color = UpPurpleBright,
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
                                tint = UpPurpleDark,
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
private fun DistrictStep(uiState: UpRollUiState, onSelected: (UpDistrict) -> Unit) {
    ChoiceScreen(
        title = "Select District",
        subtitle = "Uttar Pradesh voter list 2003",
        queryPlaceholder = "Search district",
        items = uiState.districts,
        itemTitle = UpDistrict::name,
        itemSubtitle = { "District ${it.id}" },
        headerIcon = Icons.Outlined.Map,
        itemIcon = Icons.Outlined.LocationOn,
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
    )
}

@Composable
private fun AssemblyStep(uiState: UpRollUiState, onSelected: (UpAssembly) -> Unit) {
    ChoiceScreen(
        title = "Select Assembly",
        subtitle = uiState.selectedDistrict?.name ?: "Choose assembly",
        queryPlaceholder = "Search assembly",
        items = uiState.assemblies,
        itemTitle = UpAssembly::displayName,
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
    uiState: UpRollUiState,
    onSelected: (UpPollingStation) -> Unit,
) {
    ChoiceScreen(
        title = "Select Polling Station",
        subtitle = uiState.selectedAssembly?.displayName ?: "Choose polling station",
        queryPlaceholder = "Search polling station",
        items = uiState.pollingStations,
        itemTitle = UpPollingStation::displayName,
        itemSubtitle = { it.pdfUrl.substringAfterLast('/') },
        headerIcon = Icons.Outlined.PictureAsPdf,
        itemIcon = Icons.Outlined.PictureAsPdf,
        loading = uiState.isLoading && uiState.pollingStations.isEmpty(),
        message = uiState.message,
        itemTitleMaxLines = Int.MAX_VALUE,
        onSelected = onSelected,
        itemTrailing = { station ->
            if (uiState.downloadingPartNumber == station.partNumber) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text(
                        text = "${uiState.downloadProgress}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = UpPurple,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.Download,
                    contentDescription = null,
                    tint = UpPurple,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    )
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
    itemTrailing: (@Composable (T) -> Unit)? = null,
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
        item(key = "up-header-$title") {
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
                HorizontalDivider(color = UpStroke)
            }
        }
        itemsIndexed(
            items = filtered,
            key = { index, item -> "up-$title-$index-${itemTitle(item)}" },
        ) { index, item ->
            ChoiceCard(
                title = itemTitle(item),
                subtitle = itemSubtitle(item),
                icon = itemIcon,
                accentIndex = index,
                titleMaxLines = itemTitleMaxLines,
                trailing = itemTrailing?.let { trailing -> { trailing(item) } },
                onClick = { onSelected(item) },
            )
        }
        item(key = "up-bottom-space-$title") { Spacer(Modifier.height(88.dp)) }
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
                    tint = UpPurple,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = UpInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = UpMuted,
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
    trailing: (@Composable () -> Unit)? = null,
) {
    val accent = Accents[accentIndex % Accents.size]
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
                    color = UpInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UpInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = UpPurple,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun CaptchaDialog(
    uiState: UpRollUiState,
    onInputChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter captcha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF4F2FF),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = uiState.captchaText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = UpPurpleBright,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh captcha")
                    }
                }
                OutlinedTextField(
                    value = uiState.captchaInput,
                    onValueChange = onInputChanged,
                    label = { Text("Captcha") },
                    singleLine = true,
                    colors = searchFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
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
    focusedBorderColor = UpStroke,
    unfocusedBorderColor = UpStroke,
    focusedLeadingIconColor = UpMuted,
    unfocusedLeadingIconColor = UpMuted,
    focusedPlaceholderColor = UpMuted,
    unfocusedPlaceholderColor = UpMuted,
    cursorColor = UpPurple,
    focusedTextColor = UpInk,
    unfocusedTextColor = UpInk,
)
