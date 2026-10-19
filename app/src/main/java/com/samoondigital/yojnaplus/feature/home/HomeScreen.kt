package com.samoondigital.yojnaplus.feature.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding

@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    onOpenDownloads: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHero(
            onOpenDownloads = onOpenDownloads,
            onOpenVoterList = onDownloadPdf,
        )

        Column(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SavedPdfCard(onOpenDownloads)
            HowToDownload()
            HomeDisclaimer()
            Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 8.dp))
        }
    }
}

@Composable
private fun HomeHero(
    onOpenDownloads: () -> Unit,
    onOpenVoterList: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF240E65), Color(0xFF351A87), Color(0xFF4522A8)),
                ),
            )
            .padding(start = 21.dp, end = 21.dp, top = 10.dp, bottom = 20.dp),
    ) {
        HeaderArtwork(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding(),
        ) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp),
                ) {
                    Text(
                        text = "Voter List 2026",
                        color = Color.White,
                        fontSize = 24.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.ui_home_help),
                        color = Color(0xFFE4D8FF),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Surface(
                    onClick = onOpenDownloads,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.ui_saved_pdfs),
                            tint = BrandPurple,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onOpenVoterList,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandPurple),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                Text(
                    stringResource(R.string.ui_download_list),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
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
            moveTo(0f, h * 0.68f)
            cubicTo(w * 0.16f, h * 0.50f, w * 0.30f, h * 0.72f, w * 0.48f, h * 0.58f)
            cubicTo(w * 0.66f, h * 0.44f, w * 0.78f, h * 0.76f, w, h * 0.55f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wave, Color(0xFF6E55DC).copy(alpha = 0.27f))

        val map = Path().apply {
            moveTo(w * 0.68f, h * 0.22f)
            cubicTo(w * 0.73f, h * 0.10f, w * 0.80f, h * 0.18f, w * 0.78f, h * 0.31f)
            cubicTo(w * 0.88f, h * 0.28f, w * 0.95f, h * 0.44f, w * 0.88f, h * 0.50f)
            cubicTo(w * 0.92f, h * 0.62f, w * 0.78f, h * 0.62f, w * 0.76f, h * 0.54f)
            cubicTo(w * 0.69f, h * 0.60f, w * 0.63f, h * 0.49f, w * 0.69f, h * 0.42f)
            cubicTo(w * 0.62f, h * 0.35f, w * 0.66f, h * 0.27f, w * 0.68f, h * 0.22f)
            close()
        }
        drawPath(map, Color.White.copy(alpha = 0.14f))
    }
}

@Composable
private fun SavedPdfCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Description, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ui_saved_pdfs), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.ui_saved_help), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = BrandPurple)
        }
    }
}

@Composable
private fun HowToDownload() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.ui_how_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.ui_how_steps), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HomeDisclaimer() {
    val uriHandler = LocalUriHandler.current
    val privacyUrl = "https://sites.google.com/view/voterlist2026/home"
    val instagramUrl = "https://www.instagram.com/samoon_digital/"
    val email = "alimailsamun@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = stringResource(R.string.ui_independent),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.ui_disclaimer),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FooterLink(
                label = stringResource(R.string.ui_privacy),
                icon = Icons.Outlined.Link,
                onClick = { uriHandler.openUri(privacyUrl) },
            )
            FooterDot()
            FooterLink(
                label = stringResource(R.string.ui_follow),
                icon = Icons.Outlined.Link,
                onClick = { uriHandler.openUri(instagramUrl) },
            )
            FooterDot()
            FooterLink(
                label = stringResource(R.string.ui_contact),
                icon = Icons.Outlined.Email,
                onClick = { uriHandler.openUri("mailto:$email") },
            )
        }
    }
}

@Composable
private fun FooterLink(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 3.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            color = Color(0xFF475569),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun FooterDot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFCBD5E1)),
    )
}


private val BrandPurple = Color(0xFF522DE0)
