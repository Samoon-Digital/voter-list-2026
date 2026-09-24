package com.samoondigital.yojnaplus.feature.home

import android.app.Activity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding

@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    onOpenDeletedList: () -> Unit,
    onOpenOldSir: () -> Unit,
    onOpenDownloads: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    HomeStatusBarEffect()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHero(onOpenDownloads = onOpenDownloads)

        Column(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionTitle()
            CategoryList(
                onRuralClick = onDownloadPdf,
                onUrbanClick = onDownloadPdf,
                onVoterListClick = onDownloadPdf,
                onDeletedClick = onOpenDeletedList,
                onOldSirClick = onOpenOldSir,
            )
            TrustBadge()
            HomeDisclaimer()
            Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 8.dp))
        }
    }
}

@Composable
private fun HomeHero(onOpenDownloads: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF240E65), Color(0xFF351A87), Color(0xFF4522A8)),
                ),
            )
            .padding(start = 21.dp, end = 21.dp, top = 8.dp, bottom = 24.dp),
    ) {
        HeaderArtwork(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp),
                ) {
                    LiveBadge()
                    Spacer(Modifier.height(8.dp))
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
                        text = "Download & search electoral roll easily.",
                        color = Color(0xFFE4D8FF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    onClick = onOpenDownloads,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Downloads",
                            tint = BrandPurple,
                            modifier = Modifier.size(25.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            NoticeBar()
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
private fun LiveBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF34D399)),
        )
        Text(
            text = "Live Electoral Portal 2026",
            color = Color(0xFFE7DDFF),
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NoticeBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "NEW",
            color = Color(0xFF1F2937),
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFACC15))
                .padding(horizontal = 6.dp, vertical = 3.dp),
        )
        Text(
            text = "Special Intensive Revision (SIR) 2026 Published",
            color = Color(0xFFF4EEFF),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFE5D9FF),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun SectionTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SELECT VOTER LIST CATEGORY",
            color = MutedText,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "5 Options",
            color = BrandPurple,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CategoryList(
    onRuralClick: () -> Unit,
    onUrbanClick: () -> Unit,
    onVoterListClick: () -> Unit,
    onDeletedClick: () -> Unit,
    onOldSirClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CategoryRow(
                title = "Gram Panchayat Voter List",
                tag = "Rural",
                description = "Village panchayat and ward-wise voter list",
                icon = Icons.Outlined.Home,
                iconTint = Color(0xFF10B981),
                iconBackground = Color(0xFFECFDF5),
                tagBackground = Color(0xFFD1FAE5),
                tagText = Color(0xFF047857),
                onClick = onRuralClick,
            )
            ListDivider()
            CategoryRow(
                title = "Urban Voter List",
                tag = "Urban",
                description = "Municipality, municipal corporation & civic body list",
                icon = Icons.Outlined.Apartment,
                iconTint = BrandPurple,
                iconBackground = Color(0xFFEEF2FF),
                tagBackground = Color(0xFFE0E7FF),
                tagText = Color(0xFF3730A3),
                onClick = onUrbanClick,
            )
            ListDivider()
            CategoryRow(
                title = "Voter List 2026",
                tag = "General 2026",
                description = "Download latest consolidated electoral roll 2026",
                icon = Icons.Outlined.Description,
                iconTint = Color(0xFF2563EB),
                iconBackground = Color(0xFFEFF6FF),
                tagBackground = Color(0xFFDBEAFE),
                tagText = Color(0xFF1D4ED8),
                onClick = onVoterListClick,
            )
            ListDivider()
            CategoryRow(
                title = "Deleted List 2026",
                tag = "Deleted",
                description = "View list of removed or struck-off voter names",
                icon = Icons.Outlined.Delete,
                iconTint = Color(0xFFF43F5E),
                iconBackground = Color(0xFFFFF1F2),
                tagBackground = Color(0xFFFFE4E6),
                tagText = Color(0xFFBE123C),
                onClick = onDeletedClick,
            )
            ListDivider()
            CategoryRow(
                title = "Old SIR List",
                tag = "SIR",
                description = "Special intensive revision electoral roll from previous enumeration",
                icon = Icons.Outlined.Schedule,
                iconTint = Color(0xFFD97706),
                iconBackground = Color(0xFFFFFBEB),
                tagBackground = Color(0xFFFEF3C7),
                tagText = Color(0xFFB45309),
                onClick = onOldSirClick,
            )
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    tag: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    tagBackground: Color,
    tagText: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(41.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(21.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    color = PrimaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                )
                Text(
                    text = tag,
                    color = tagText,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagBackground)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
            Text(
                text = description,
                color = SecondaryText,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun ListDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color(0xFFF1F5F9),
        modifier = Modifier.padding(start = 67.dp),
    )
}

@Composable
private fun TrustBadge() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF6F0FF),
        border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE9D5FF)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp, end = 8.dp),
            ) {
                Text(
                    text = "100% Secure & Direct Access",
                    color = PrimaryText,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Reference portal based on official electoral sources",
                    color = SecondaryText,
                    fontSize = 10.5.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "Verified",
                color = BrandPurple,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
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
                text = "DISCLAIMER",
                color = Color(0xFF334155),
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "This application is for informational reference only and is not affiliated with or endorsed by the Election Commission or any government entity.",
                color = SecondaryText,
                fontSize = 11.sp,
                lineHeight = 17.sp,
            )
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FooterLink(
                label = "Privacy Policy",
                icon = Icons.Outlined.Link,
                onClick = { uriHandler.openUri(privacyUrl) },
            )
            FooterDot()
            FooterLink(
                label = "Follow Us",
                icon = Icons.Outlined.Link,
                onClick = { uriHandler.openUri(instagramUrl) },
            )
            FooterDot()
            FooterLink(
                label = "Contact / Email",
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
            fontSize = 11.sp,
            lineHeight = 13.sp,
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

@Composable
private fun HomeStatusBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window == null || view.isInEditMode) {
            onDispose { }
        } else {
            @Suppress("DEPRECATION")
            val previousColor = window.statusBarColor
            val controller = WindowInsetsControllerCompat(window, view)
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            @Suppress("DEPRECATION")
            window.statusBarColor = Color(0xFF240E65).toArgb()
            controller.isAppearanceLightStatusBars = false

            onDispose {
                @Suppress("DEPRECATION")
                window.statusBarColor = previousColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }
}

private val HomeBackground = Color(0xFFF8FAFC)
private val BrandPurple = Color(0xFF522DE0)
private val PrimaryText = Color(0xFF0F172A)
private val SecondaryText = Color(0xFF64748B)
private val MutedText = Color(0xFF94A3B8)
