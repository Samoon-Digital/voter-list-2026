package com.samoondigital.yojnaplus.feature.oldsir

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.alpha
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
import com.samoondigital.yojnaplus.core.ui.components.LazyNativeAdItem
import com.samoondigital.yojnaplus.model.OldSirAssemblyDto
import com.samoondigital.yojnaplus.model.OldSirDistrictDto
import com.samoondigital.yojnaplus.model.OldSirPartDto
import com.samoondigital.yojnaplus.model.StateDto

private val OldSirPurple = Color(0xFF3522A8)
private val OldSirPurpleDark = Color(0xFF20106F)
private val OldSirPurpleBright = Color(0xFF7C5CFF)
private val OldSirInk = Color(0xFF090B1F)
private val OldSirMuted = Color(0xFF686A8D)
private val OldSirSurface = Color(0xFFFCFCFF)
private val OldSirStroke = Color(0xFFE3E2F5)
private const val NativeAdInterval = 7
private const val UttarPradeshStateCd = "S24"
private const val JammuKashmirStateCd = "U08"
private val ChoiceAccents = listOf(
    Color(0xFF4A2CC3),
    Color(0xFF43A66E),
    Color(0xFFD66C2E),
    Color(0xFF477CCA),
    Color(0xFFC83D77),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OldSirScreen(
    onBack: () -> Unit,
    onOpenUttarPradesh: () -> Unit,
    onOpenJammuKashmir: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OldSirViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is OldSirEvent.OpenPdf -> onOpenPdf(event.uri, event.title)
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
            OldSirTopBar(
                uiState = uiState,
                onBack = ::handleBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(OldSirSurface),
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "old-sir-step",
            ) { step ->
                when (step) {
                    OldSirStep.State -> StateStep(
                        uiState = uiState,
                        onSelected = { state ->
                            when (state.stateCd) {
                                UttarPradeshStateCd -> onOpenUttarPradesh()
                                JammuKashmirStateCd -> onOpenJammuKashmir()
                                else -> viewModel.selectState(state)
                            }
                        },
                        itemEnabled = { state ->
                            state.stateCd == UttarPradeshStateCd ||
                                state.stateCd == JammuKashmirStateCd ||
                                viewModel.isStateSupported(state)
                        },
                    )
                    OldSirStep.District -> DistrictStep(uiState, viewModel::selectDistrict)
                    OldSirStep.Assembly -> AssemblyStep(uiState, viewModel::selectAssembly)
                    OldSirStep.PollingStation -> PollingStationStep(
                        uiState = uiState,
                        onSelected = viewModel::openPartPdf,
                    )
                }
            }
        }
    }
}

@Composable
private fun OldSirTopBar(
    uiState: OldSirUiState,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(OldSirPurpleDark, OldSirPurple, Color(0xFF2E1B98)),
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
                        tint = OldSirPurpleDark,
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
                    text = "Old SIR List",
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
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = OldSirPurpleDark,
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
                                color = OldSirPurpleBright,
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
                                tint = OldSirPurpleDark,
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
private fun StateStep(
    uiState: OldSirUiState,
    onSelected: (StateDto) -> Unit,
    itemEnabled: (StateDto) -> Boolean,
) {
    SearchableChoiceScreen(
        title = "Select State",
        subtitle = "Choose the state where the old SIR roll is published.",
        queryPlaceholder = "Search state",
        items = uiState.states,
        itemTitle = StateDto::stateName,
        itemSubtitle = { null },
        headerIcon = Icons.Outlined.Map,
        itemIcon = Icons.Outlined.Map,
        loading = uiState.isLoading && uiState.states.isEmpty(),
        message = uiState.message,
        showSearch = false,
        itemEnabled = itemEnabled,
        onSelected = onSelected,
    )
}

@Composable
private fun DistrictStep(uiState: OldSirUiState, onSelected: (OldSirDistrictDto) -> Unit) {
    SearchableChoiceScreen(
        title = "Select District",
        subtitle = uiState.selectedState?.stateName ?: "Choose district",
        queryPlaceholder = "Search district",
        items = uiState.districts,
        itemTitle = OldSirDistrictDto::displayName,
        itemSubtitle = { "District ${it.districtNo}" },
        headerIcon = Icons.Outlined.LocationOn,
        itemIcon = Icons.Outlined.LocationOn,
        loading = uiState.isLoading && uiState.districts.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
    )
}

@Composable
private fun AssemblyStep(uiState: OldSirUiState, onSelected: (OldSirAssemblyDto) -> Unit) {
    SearchableChoiceScreen(
        title = "Select Assembly Constituency",
        subtitle = uiState.selectedDistrict?.displayName ?: "Choose assembly constituency",
        queryPlaceholder = "Search assembly",
        items = uiState.assemblies,
        itemTitle = OldSirAssemblyDto::displayName,
        itemSubtitle = { it.acType?.takeIf(String::isNotBlank) },
        headerIcon = Icons.Outlined.AccountBalance,
        itemIcon = Icons.Outlined.AccountBalance,
        loading = uiState.isLoading && uiState.assemblies.isEmpty(),
        message = uiState.message,
        onSelected = onSelected,
    )
}

@Composable
private fun PollingStationStep(
    uiState: OldSirUiState,
    onSelected: (OldSirPartDto) -> Unit,
) {
    SearchableChoiceScreen(
        title = "Select Polling Station",
        subtitle = uiState.selectedAssembly?.displayName ?: "Choose polling station",
        queryPlaceholder = "Search polling station",
        items = uiState.parts,
        itemTitle = OldSirPartDto::displayName,
        itemSubtitle = { it.oldPdfUrl?.substringAfterLast('/') },
        headerIcon = Icons.Outlined.PictureAsPdf,
        itemIcon = Icons.Outlined.PictureAsPdf,
        loading = uiState.isLoading && uiState.parts.isEmpty(),
        message = uiState.message,
        itemTitleMaxLines = Int.MAX_VALUE,
        onSelected = onSelected,
        itemTrailing = { part ->
            if (uiState.downloadingPartNumber == part.partNumber) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        text = "${uiState.downloadProgress}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = OldSirPurple,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    tint = OldSirPurple,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    )
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
    itemTitleMaxLines: Int = 1,
    itemEnabled: (T) -> Boolean = { true },
    headerIcon: ImageVector,
    itemIcon: ImageVector,
    itemTrailing: (@Composable (T) -> Unit)? = null,
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
        trailingHeader = if (showSearch) {
            {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Outlined.Tune, contentDescription = null, tint = OldSirPurple) },
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
    ) { listState ->
        filtered.forEachIndexed { index, item ->
            val titleKey = itemTitle(item)
            item(key = "old-sir-$title-$index-$titleKey") {
                ChoiceCard(
                    title = titleKey,
                    subtitle = itemSubtitle(item),
                    icon = itemIcon,
                    accentIndex = index,
                    titleMaxLines = itemTitleMaxLines,
                    enabled = itemEnabled(item),
                    trailing = itemTrailing?.let { trailing -> { trailing(item) } },
                    onClick = { onSelected(item) },
                )
            }
            NativeAdInsertion(
                listState = listState,
                prefix = title,
                index = index,
                suffix = titleKey,
            )
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
    trailingHeader: (@Composable ColumnScope.() -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.(LazyListState) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "old-sir-header-$title") {
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

                trailingHeader?.invoke(this)
                HorizontalDivider(color = OldSirStroke)
            }
        }
        content(listState)
        item(key = "old-sir-bottom-space-$title") { Spacer(Modifier.height(88.dp)) }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.NativeAdInsertion(
    listState: LazyListState,
    prefix: String,
    index: Int,
    suffix: Any,
) {
    if ((index + 1) % NativeAdInterval != 0) return
    val adItemKey = "old-sir-native-$prefix-${index + 1}-$suffix"
    item(key = adItemKey) {
        LazyNativeAdItem(
            listState = listState,
            itemKey = adItemKey,
            placementKey = adItemKey,
        )
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
                    tint = OldSirPurple,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OldSirInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = OldSirMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OldSirStroke,
    unfocusedBorderColor = OldSirStroke,
    focusedLeadingIconColor = OldSirMuted,
    unfocusedLeadingIconColor = OldSirMuted,
    focusedPlaceholderColor = OldSirMuted,
    unfocusedPlaceholderColor = OldSirMuted,
    cursorColor = OldSirPurple,
    focusedTextColor = OldSirInk,
    unfocusedTextColor = OldSirInk,
)

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    icon: ImageVector,
    accentIndex: Int,
    titleMaxLines: Int = 1,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    val accent = ChoiceAccents[accentIndex % ChoiceAccents.size]
    ElevatedCard(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
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
                    color = OldSirInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OldSirInk,
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
                    tint = OldSirPurple,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
