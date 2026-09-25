package com.samoondigital.yojnaplus.feature.upcurrent

import android.app.Activity
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding

private val UpPurple = Color(0xFF3522A8)
private val UpPurpleDark = Color(0xFF20106F)
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

@Composable
fun UpStateListScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenUttarPradesh: () -> Unit,
    onOpenBihar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val stateCount = if (onOpenBihar == null) 1 else 2
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SimpleUpTopBar(
                title = title,
                subtitle = "$stateCount states available",
                onBack = onBack,
                onOpenDownloads = null,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(UpSurface),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                StepHeading(
                    icon = Icons.Outlined.Map,
                    title = "Select State",
                    subtitle = subtitle,
                )
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = UpStroke)
            }
            item {
                ChoiceRow(
                    title = "Uttar Pradesh",
                    subtitle = "Live official SEC flow",
                    icon = Icons.Outlined.LocationOn,
                    accentIndex = 0,
                    onClick = onOpenUttarPradesh,
                )
            }
            onOpenBihar?.let { openBihar ->
                item {
                    ChoiceRow(
                        title = "Bihar",
                        subtitle = "Municipality voter list 2026",
                        icon = Icons.Outlined.LocationOn,
                        accentIndex = 1,
                        onClick = openBihar,
                    )
                }
            }
        }
    }
}

@Composable
fun UpRuralVoterListScreen(
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpRuralVoterListViewModel = hiltViewModel(),
) {
    UpCurrentFlowScreen(
        title = "UP Gram Panchayat",
        viewModel = viewModel,
        onBack = onBack,
        onOpenDownloads = onOpenDownloads,
        onOpenPdf = onOpenPdf,
        modifier = modifier,
    )
}

@Composable
fun UpUrbanVoterListScreen(
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpUrbanVoterListViewModel = hiltViewModel(),
) {
    UpCurrentFlowScreen(
        title = "UP Urban Voter List",
        viewModel = viewModel,
        onBack = onBack,
        onOpenDownloads = onOpenDownloads,
        onOpenPdf = onOpenPdf,
        modifier = modifier,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun UpCurrentFlowScreen(
    title: String,
    viewModel: UpCurrentViewModel,
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UpCurrentEvent.OpenPdf -> onOpenPdf(event.uri, event.title)
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
            SimpleUpTopBar(
                title = title,
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
                .background(UpSurface),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "up-current-step",
            ) { step ->
                when (step) {
                    UpCurrentStep.District -> OptionStep(
                        title = "Select District",
                        subtitle = "Uttar Pradesh gram panchayat voter list",
                        queryPlaceholder = "Search district",
                        items = uiState.districts,
                        loading = uiState.isLoading && uiState.districts.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Map,
                        itemIcon = Icons.Outlined.LocationOn,
                        onSelected = viewModel::selectRuralDistrict,
                    )
                    UpCurrentStep.Block -> OptionStep(
                        title = "Select Block",
                        subtitle = uiState.selectedDistrict?.label ?: "Choose block",
                        queryPlaceholder = "Search block",
                        items = uiState.blocks,
                        loading = uiState.isLoading && uiState.blocks.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.AccountBalance,
                        itemIcon = Icons.Outlined.AccountBalance,
                        onSelected = viewModel::selectRuralBlock,
                    )
                    UpCurrentStep.GramPanchayat -> OptionStep(
                        title = "Select Gram Panchayat",
                        subtitle = uiState.selectedBlock?.label ?: "Choose gram panchayat",
                        queryPlaceholder = "Search gram panchayat",
                        items = uiState.gramPanchayats,
                        loading = uiState.isLoading && uiState.gramPanchayats.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Home,
                        itemIcon = Icons.Outlined.Home,
                        onSelected = { option ->
                            InterstitialAdManager.showIfAvailable(activity) {
                                viewModel.selectRuralGramPanchayat(option)
                            }
                        },
                    )
                    UpCurrentStep.UrbanBodyType -> OptionStep(
                        title = "Select Urban Body Type",
                        subtitle = "Municipality and corporation voter list",
                        queryPlaceholder = "Search type",
                        items = uiState.urbanBodyTypes,
                        loading = uiState.isLoading && uiState.urbanBodyTypes.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Apartment,
                        itemIcon = Icons.Outlined.Apartment,
                        onSelected = viewModel::selectUrbanBodyType,
                    )
                    UpCurrentStep.UrbanDistrict -> OptionStep(
                        title = "Select District",
                        subtitle = uiState.selectedUrbanBodyType?.label ?: "Choose district",
                        queryPlaceholder = "Search district",
                        items = uiState.urbanDistricts,
                        loading = uiState.isLoading && uiState.urbanDistricts.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Map,
                        itemIcon = Icons.Outlined.LocationOn,
                        onSelected = viewModel::selectUrbanDistrict,
                    )
                    UpCurrentStep.UrbanUlb -> OptionStep(
                        title = "Select Urban Local Body",
                        subtitle = uiState.selectedUrbanDistrict?.label ?: "Choose urban local body",
                        queryPlaceholder = "Search ULB",
                        items = uiState.urbanUlbs,
                        loading = uiState.isLoading && uiState.urbanUlbs.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.LocationCity,
                        itemIcon = Icons.Outlined.LocationCity,
                        onSelected = { option ->
                            InterstitialAdManager.showIfAvailable(activity) {
                                viewModel.selectUrbanUlb(option)
                            }
                        },
                    )
                    UpCurrentStep.UrbanWard -> OptionStep(
                        title = "Select Ward",
                        subtitle = uiState.selectedUrbanUlb?.label ?: "Choose ward",
                        queryPlaceholder = "Search ward",
                        items = uiState.urbanWards,
                        loading = uiState.isLoading && uiState.urbanWards.isEmpty(),
                        message = uiState.message,
                        headerIcon = Icons.Outlined.Home,
                        itemIcon = Icons.Outlined.Home,
                        onSelected = { option ->
                            InterstitialAdManager.showIfAvailable(activity) {
                                viewModel.selectUrbanWard(option)
                            }
                        },
                    )
                    UpCurrentStep.Captcha -> CaptchaStep(
                        uiState = uiState,
                        onCaptchaChange = viewModel::updateCaptchaInput,
                        onRefresh = viewModel::refreshCaptcha,
                        onSubmit = viewModel::submitCaptcha,
                        onBack = ::handleBack,
                    )
                    UpCurrentStep.UrbanPdfType -> UrbanDownloadOptionsStep(
                        uiState = uiState,
                        onDownload = viewModel::downloadUrbanPdf,
                        onBack = ::handleBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleUpTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenDownloads: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
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
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = UpPurpleDark,
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
            if (onOpenDownloads != null) {
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
                            tint = UpPurpleDark,
                            modifier = Modifier.size(22.dp),
                        )
                    }
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
    items: List<UpSecOption>,
    loading: Boolean,
    message: String?,
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    onSelected: (UpSecOption) -> Unit,
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
                HorizontalDivider(color = UpStroke)
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
                Icon(icon, contentDescription = null, tint = UpPurple, modifier = Modifier.size(28.dp))
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
private fun ChoiceRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    accentIndex: Int,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val accent = Accents[accentIndex % Accents.size]
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
                    color = UpInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = UpMuted,
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
                    tint = UpPurple,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(color = UpStroke)
    }
}

@Composable
private fun CaptchaStep(
    uiState: UpCurrentUiState,
    onCaptchaChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val captchaBitmap = remember(uiState.captchaBytes) {
        uiState.captchaBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    val label = when (uiState.flow) {
        UpCurrentFlow.Rural -> uiState.selectedGramPanchayat?.label
        UpCurrentFlow.Urban -> uiState.selectedUrbanWard?.label
    }.orEmpty().ifBlank { "Voter List" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StepHeading(Icons.Outlined.PictureAsPdf, "Enter Captcha", label)
        uiState.message?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, UpStroke),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (captchaBitmap != null) {
                        Image(
                            bitmap = captchaBitmap,
                            contentDescription = "Captcha",
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .background(Color.White),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading && !uiState.isDownloading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh captcha", tint = UpPurple)
                    }
                }
                OutlinedTextField(
                    value = uiState.captchaInput,
                    onValueChange = onCaptchaChange,
                    label = { Text("Captcha") },
                    singleLine = true,
                    colors = searchFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (uiState.isDownloading || uiState.downloadProgress > 0) {
            LinearProgressIndicator(
                progress = { uiState.downloadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onSubmit,
            enabled = uiState.captchaBytes != null &&
                uiState.captchaInput.isNotBlank() &&
                !uiState.isLoading &&
                !uiState.isDownloading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Icon(Icons.Outlined.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (uiState.isDownloading) "Downloading..." else "Download PDF")
        }
        OutlinedButton(
            onClick = onBack,
            enabled = !uiState.isDownloading,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
        ) {
            Text("Back to Selection")
        }
    }
}

@Composable
private fun UrbanDownloadOptionsStep(
    uiState: UpCurrentUiState,
    onDownload: (UpUrbanDownloadOption) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item {
            StepHeading(
                icon = Icons.Outlined.PictureAsPdf,
                title = "Select PDF Type",
                subtitle = uiState.selectedUrbanWard?.label ?: "Available voter lists",
            )
            uiState.message?.let {
                Spacer(Modifier.height(10.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (uiState.isDownloading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { uiState.downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = UpStroke)
        }
        uiState.urbanDownloadOptions.forEachIndexed { index, option ->
            item(key = option.id) {
                ChoiceRow(
                    title = option.label,
                    subtitle = option.value,
                    icon = Icons.Outlined.PictureAsPdf,
                    accentIndex = index,
                    onClick = { if (!uiState.isDownloading) onDownload(option) },
                    trailing = {
                        if (uiState.selectedUrbanDownloadOption?.id == option.id && uiState.isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Icon(Icons.Outlined.Download, contentDescription = null, tint = UpPurple)
                        }
                    },
                )
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBack,
                enabled = !uiState.isDownloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
            ) {
                Text("Back to Captcha")
            }
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
