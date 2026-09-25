package com.samoondigital.yojnaplus.feature.biharurban

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding

private val BiharPurple = Color(0xFF3522A8)
private val BiharPurpleDark = Color(0xFF20106F)
private val BiharInk = Color(0xFF090B1F)
private val BiharMuted = Color(0xFF686A8D)
private val BiharSurface = Color(0xFFFCFCFF)
private val BiharStroke = Color(0xFFE3E2F5)
private val BiharAccents = listOf(
    Color(0xFF4A2CC3),
    Color(0xFF43A66E),
    Color(0xFFD66C2E),
    Color(0xFF477CCA),
    Color(0xFFC83D77),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BiharUrbanScreen(
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiharUrbanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BiharUrbanEvent.OpenPdf -> onOpenPdf(event.uri, event.title)
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
            BiharTopBar(
                title = "Bihar Urban Voter List",
                subtitle = "Step ${uiState.stepNumber} of ${uiState.totalSteps}",
                onBack = ::handleBack,
                onOpenDownloads = onOpenDownloads,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BiharSurface),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "bihar-urban-step",
            ) { step ->
                when (step) {
                    BiharUrbanStep.District -> OptionStep(
                        title = "Select District",
                        subtitle = "Bihar municipality voter list",
                        queryPlaceholder = "Search district",
                        items = uiState.districts,
                        loading = uiState.isLoading && uiState.districts.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Map,
                        itemIcon = Icons.Outlined.LocationOn,
                        onSelected = viewModel::selectDistrict,
                    )
                    BiharUrbanStep.Subdivision -> OptionStep(
                        title = "Select Subdivision",
                        subtitle = uiState.selectedDistrict?.label ?: "Choose subdivision",
                        queryPlaceholder = "Search subdivision",
                        items = uiState.subdivisions,
                        loading = uiState.isLoading && uiState.subdivisions.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.LocationCity,
                        itemIcon = Icons.Outlined.LocationCity,
                        onSelected = viewModel::selectSubdivision,
                    )
                    BiharUrbanStep.Municipality -> OptionStep(
                        title = "Select Municipality",
                        subtitle = uiState.selectedSubdivision?.label ?: "Choose municipality",
                        queryPlaceholder = "Search municipality",
                        items = uiState.municipalities,
                        loading = uiState.isLoading && uiState.municipalities.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Apartment,
                        itemIcon = Icons.Outlined.Apartment,
                        onSelected = { option ->
                            InterstitialAdManager.showIfAvailable(activity) {
                                viewModel.selectMunicipality(option)
                            }
                        },
                    )
                    BiharUrbanStep.PdfList -> PdfListStep(
                        uiState = uiState,
                        onDownload = viewModel::downloadPdf,
                    )
                }
            }
        }
    }
}

@Composable
private fun BiharTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(BiharPurpleDark, BiharPurple, Color(0xFF2E1B98)),
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
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BiharPurpleDark,
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
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = "Open downloads",
                        tint = BiharPurpleDark,
                        modifier = Modifier.size(22.dp),
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
private fun OptionStep(
    title: String,
    subtitle: String,
    queryPlaceholder: String,
    items: List<BiharUrbanOption>,
    loading: Boolean,
    message: String?,
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    onSelected: (BiharUrbanOption) -> Unit,
) {
    var query by remember(title) { mutableStateOf("") }
    val filtered = remember(items, query) {
        val term = query.trim()
        if (term.isBlank()) items else items.filter {
            it.label.matchesSearchQuery(term) || it.value.matchesSearchQuery(term)
        }
    }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "header-$title") {
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
                HorizontalDivider(color = BiharStroke)
            }
        }
        filtered.forEachIndexed { index, item ->
            item(key = "$title-$index-${item.value}") {
                ChoiceRow(
                    title = item.label,
                    subtitle = item.value.takeIf { it.isNotBlank() },
                    icon = itemIcon,
                    accentIndex = index,
                    onClick = { onSelected(item) },
                )
            }
        }
        item(key = "bottom-$title") { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun PdfListStep(
    uiState: BiharUrbanUiState,
    onDownload: (BiharUrbanPdfLink) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "pdf-header") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StepHeading(
                    icon = Icons.Outlined.PictureAsPdf,
                    title = "Select Ward PDF",
                    subtitle = uiState.selectedMunicipality?.label ?: "Available voter lists",
                )
                if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (uiState.isDownloading) {
                    LinearProgressIndicator(
                        progress = { uiState.downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                uiState.message?.takeUnless { uiState.isLoading }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isDownloading) BiharMuted else MaterialTheme.colorScheme.error,
                    )
                }
                HorizontalDivider(color = BiharStroke)
            }
        }
        uiState.pdfLinks.forEachIndexed { index, link ->
            item(key = link.url) {
                ChoiceRow(
                    title = link.label,
                    subtitle = link.fileName,
                    icon = Icons.Outlined.PictureAsPdf,
                    accentIndex = index,
                    onClick = { if (!uiState.isDownloading) onDownload(link) },
                    trailing = {
                        if (uiState.downloadingUrl == link.url && uiState.isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Icon(Icons.Outlined.Download, contentDescription = null, tint = BiharPurple)
                        }
                    },
                )
            }
        }
        item(key = "pdf-bottom") { Spacer(Modifier.height(88.dp)) }
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
                Icon(icon, contentDescription = null, tint = BiharPurple, modifier = Modifier.size(28.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BiharInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = BiharMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    accentIndex: Int,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val accent = BiharAccents[accentIndex % BiharAccents.size]
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
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BiharInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BiharMuted,
                        maxLines = 1,
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
                    tint = BiharPurple,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(color = BiharStroke)
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
    focusedBorderColor = BiharStroke,
    unfocusedBorderColor = BiharStroke,
    focusedLeadingIconColor = BiharMuted,
    unfocusedLeadingIconColor = BiharMuted,
    focusedPlaceholderColor = BiharMuted,
    unfocusedPlaceholderColor = BiharMuted,
    cursorColor = BiharPurple,
    focusedTextColor = BiharInk,
    unfocusedTextColor = BiharInk,
)
