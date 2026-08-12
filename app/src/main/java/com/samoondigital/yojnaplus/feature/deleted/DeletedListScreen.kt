package com.samoondigital.yojnaplus.feature.deleted

import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samoondigital.yojnaplus.core.ui.components.LazyNativeAdItem

private val DeletedPurple = Color(0xFF3522A8)
private val DeletedPurpleDark = Color(0xFF20106F)
private val DeletedInk = Color(0xFF090B1F)
private val DeletedMuted = Color(0xFF686A8D)
private val DeletedSurface = Color(0xFFFCFCFF)
private val DeletedStroke = Color(0xFFE3E2F5)
private val DeletedRed = Color(0xFFD92525)
private const val NativeAdInterval = 7
private val ChoiceAccents = listOf(
    Color(0xFFD92525),
    Color(0xFF2466E8),
    Color(0xFF168548),
    Color(0xFFD66C2E),
    Color(0xFF7B4DC8),
)

data class DeletedListLink(
    val id: String,
    val stateName: String,
    val url: String?,
    val unavailableReason: String? = null,
) {
    val isAvailable: Boolean = !url.isNullOrBlank()
}

object DeletedListLinks {
    val all: List<DeletedListLink> = listOf(
        DeletedListLink("andhra-pradesh", "Andhra Pradesh", "https://ceoaperolls.ap.gov.in/PublishASDD/"),
        DeletedListLink("arunachal-pradesh", "Arunachal Pradesh", null, "Not available"),
        DeletedListLink("assam", "Assam", null, "Not available"),
        DeletedListLink("bihar", "Bihar", "https://ceoelection.bihar.gov.in/electors_included_till_24_06_2025_not_in_draft_01_08_2025.html"),
        DeletedListLink("chhattisgarh", "Chhattisgarh", "https://election.cg.gov.in/ASDList/"),
        DeletedListLink("goa", "Goa", "https://ceogoa.nic.in/appln/UIL/DraftRollSearch.aspx"),
        DeletedListLink("gujarat", "Gujarat", "https://chunavsetu-search.gujarat.gov.in/ADS/SearchADSEPIC.aspx"),
        DeletedListLink("haryana", "Haryana", "https://ceoharyana.gov.in/WebCMS/Start/1988"),
        DeletedListLink("himachal-pradesh", "Himachal Pradesh", null, "Not working"),
        DeletedListLink("jharkhand", "Jharkhand", "https://ceojh.jharkhand.gov.in/mrollpdf1/acengasdd.aspx"),
        DeletedListLink("karnataka", "Karnataka", "https://ceo.karnataka.gov.in/asddo.html"),
        DeletedListLink("kerala", "Kerala", "http://webapp.ceo.kerala.gov.in/duplielectors.html"),
        DeletedListLink("madhya-pradesh", "Madhya Pradesh", "https://ceoelection.mp.gov.in/SearchDraftRoll.aspx"),
        DeletedListLink("maharashtra", "Maharashtra", "https://ceoelection.maharashtra.gov.in/SearchInfo/SearchDeletionPDFmonthyear.aspx"),
        DeletedListLink("manipur", "Manipur", "https://ceomanipur.nic.in/sir_report"),
        DeletedListLink("meghalaya", "Meghalaya", "https://ceomeghalaya.nic.in/erolls/asdd/asdd.html"),
        DeletedListLink("mizoram", "Mizoram", "https://ceo.mizoram.gov.in/asdd-by-part"),
        DeletedListLink("nagaland", "Nagaland", null, "Not available"),
        DeletedListLink("odisha", "Odisha", "https://ceoodisha.nic.in/en/bloblamom/"),
        DeletedListLink("punjab", "Punjab", null, "Not available"),
        DeletedListLink("rajasthan", "Rajasthan", "https://election.rajasthan.gov.in/ASD_SIR_2026/ASD_List_EPIC.html"),
        DeletedListLink("sikkim", "Sikkim", "https://ceo.sikkim.gov.in/ElectoralRolls/PollingStationList"),
        DeletedListLink("tamil-nadu", "Tamil Nadu", null, "Not available"),
        DeletedListLink("telangana", "Telangana", "https://ceotserms2.telangana.gov.in/eroll_add_mod_del/eroll_add_mod_del.aspx"),
        DeletedListLink("uttar-pradesh", "Uttar Pradesh", "https://ceouttarpradesh.nic.in/ASD_SIR2026.aspx"),
        DeletedListLink("uttarakhand", "Uttarakhand", "https://election.uk.gov.in/asdlist"),
        DeletedListLink("andaman-nicobar", "Andaman and Nicobar Islands", "https://ceo.andamannicobar.gov.in/sir2026/Search"),
        DeletedListLink("chandigarh", "Chandigarh", null, "Not available"),
        DeletedListLink("dadra-daman-diu", "Dadra and Nagar Haveli and Daman and Diu", "https://ceodaman.nic.in/ceoasddsir2026.html"),
        DeletedListLink("jammu-kashmir", "Jammu and Kashmir", null, "Not available"),
        DeletedListLink("ladakh", "Ladakh", null, "Not available"),
        DeletedListLink("lakshadweep", "Lakshadweep", "https://ceolakshadweep.gov.in/sir_2026_asd"),
        DeletedListLink("puducherry", "Puducherry", "https://ceopuducherry.py.gov.in/voters2025/"),
    )

    fun find(id: String?): DeletedListLink? = all.firstOrNull { it.id == id }
}

@Composable
fun DeletedListScreen(
    onBack: () -> Unit,
    onOpenState: (DeletedListLink) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var query by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val filtered = remember(query) {
        val term = query.normalizedForSearch()
        if (term.isBlank()) DeletedListLinks.all else DeletedListLinks.all.filter {
            it.stateName.normalizedForSearch().contains(term)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { DeletedListTopBar(onBack = onBack) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DeletedSurface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "deleted-list-header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StepHeading(
                        title = "Deleted List",
                        subtitle = "Select a state to open the official ASD / deleted-list website inside the app.",
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = { Text("Search state") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = searchFieldColors(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    )
                    HorizontalDivider(color = DeletedStroke)
                }
            }
            filtered.forEachIndexed { index, item ->
                item(key = item.id) {
                    DeletedStateCard(
                        link = item,
                        accentIndex = index,
                        onClick = { if (item.isAvailable) onOpenState(item) },
                    )
                }
                NativeAdInsertion(
                    listState = listState,
                    index = index,
                    suffix = "${query.ifBlank { "all" }}-${filtered.size}",
                )
            }
            item(key = "deleted-list-bottom-space") { Spacer(Modifier.height(88.dp)) }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.NativeAdInsertion(
    listState: LazyListState,
    index: Int,
    suffix: Any,
) {
    if ((index + 1) % NativeAdInterval != 0) return
    val adItemKey = "deleted-list-native-${index + 1}-$suffix"
    item(key = adItemKey) {
        LazyNativeAdItem(
            listState = listState,
            itemKey = adItemKey,
            placementKey = adItemKey,
        )
    }
}

@Composable
private fun DeletedListTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(DeletedPurpleDark, DeletedPurple, Color(0xFF2E1B98)),
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
                        tint = DeletedPurpleDark,
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
                    text = "Deleted List",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Official ASD state links",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
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
private fun StepHeading(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFECEC),
            modifier = Modifier.size(50.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = DeletedRed,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DeletedInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = DeletedMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DeletedStateCard(
    link: DeletedListLink,
    accentIndex: Int,
    onClick: () -> Unit,
) {
    val accent = if (link.isAvailable) ChoiceAccents[accentIndex % ChoiceAccents.size] else DeletedMuted
    ElevatedCard(
        onClick = onClick,
        enabled = link.isAvailable,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (link.isAvailable) 1f else 0.55f)
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
                .heightIn(min = 70.dp)
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
                        if (link.isAvailable) Icons.Outlined.Language else Icons.Outlined.Block,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(27.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.stateName,
                    style = MaterialTheme.typography.titleMedium,
                    color = DeletedInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = link.url ?: link.unavailableReason.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (link.isAvailable) DeletedMuted else MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (link.isAvailable) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = DeletedPurple,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DeletedStroke,
    unfocusedBorderColor = DeletedStroke,
    focusedLeadingIconColor = DeletedMuted,
    unfocusedLeadingIconColor = DeletedMuted,
    focusedPlaceholderColor = DeletedMuted,
    unfocusedPlaceholderColor = DeletedMuted,
    cursorColor = DeletedPurple,
    focusedTextColor = DeletedInk,
    unfocusedTextColor = DeletedInk,
)

private fun String.normalizedForSearch(): String = lowercase()
    .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()
