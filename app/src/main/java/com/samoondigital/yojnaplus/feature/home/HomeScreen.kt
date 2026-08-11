package com.samoondigital.yojnaplus.feature.home

import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.AdMobNativeAd
import com.samoondigital.yojnaplus.core.ui.theme.Green
import com.samoondigital.yojnaplus.core.ui.theme.GreenContainer
import com.samoondigital.yojnaplus.notifications.NotificationPermissionPrompt

@Composable
fun HomeScreen(
    onDownloadPdf: () -> Unit,
    onOpenOldSir: () -> Unit,
    onOpenDownloads: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NotificationPermissionPrompt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHeader(
            onOpenDownloads = onOpenDownloads,
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VoterListPdfCard(
                    onClick = onDownloadPdf,
                    modifier = Modifier.weight(1f),
                )
                DeletedList2026Card(
                    onClick = onDownloadPdf,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            OldSirListCard(onClick = onOpenOldSir)
            Spacer(Modifier.height(16.dp))
            AdMobNativeAd(placementKey = "home-native-between-cards")
            Spacer(Modifier.height(18.dp))
            SecureReliableCard()
            Spacer(Modifier.height(14.dp))
            LegalContactCard()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onOpenDownloads: () -> Unit,
) {
    HomeStatusBarEffect()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(HomePurpleDark, HomePurple, Color(0xFF2E1B98)),
                    start = Offset.Zero,
                    end = Offset(950f, 360f),
                ),
            ),
    ) {
        HomeHeaderArtwork(modifier = Modifier.matchParentSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding()
                .padding(start = 18.dp, top = 10.dp, end = 18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp, end = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    maxLines = 2,
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
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = "Downloads",
                        tint = HomePurpleDark,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderArtwork(modifier: Modifier = Modifier) {
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
            window.statusBarColor = HomePurpleDark.toArgb()
            controller.isAppearanceLightStatusBars = false

            onDispose {
                @Suppress("DEPRECATION")
                window.statusBarColor = previousColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }
}

private val DeletedRed = Color(0xFFD92525)
private val DeletedRedDark = Color(0xFF7B0505)
private val VoterBlue = Color(0xFF2466E8)

@Composable
private fun DeletedList2026Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopListActionCard(
        title = "Deleted List",
        year = "2026",
        description = "Check removed names quickly.",
        actionLabel = "Check Deleted List",
        accent = DeletedRed,
        accentDark = DeletedRedDark,
        icon = Icons.Outlined.Search,
        statusIcon = Icons.Outlined.Close,
        onClick = onClick,
        modifier = modifier,
    )
}
@Composable
private fun TopListActionCard(
    title: String,
    year: String,
    description: String,
    actionLabel: String,
    accent: Color,
    accentDark: Color,
    icon: ImageVector,
    statusIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(232.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.26f)),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ActionCardBackdrop(accent = accent, modifier = Modifier.matchParentSize())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp),
                ) {
                    ClipboardCardClipart(
                        accent = accent,
                        statusIcon = statusIcon,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(78.dp),
                    )
                    YearPill(
                        year = year,
                        accent = accent,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = title,
                    color = HomePurpleDark,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = year,
                    color = accentDark,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(3.dp)
                        .background(accent.copy(alpha = 0.75f), RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = description,
                    color = Color(0xFF343B4D),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                SmallCardActionBar(
                    label = actionLabel,
                    icon = icon,
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun WideListActionCard(
    title: String,
    year: String,
    description: String,
    actionLabel: String,
    accent: Color,
    accentDark: Color,
    icon: ImageVector,
    statusIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.26f)),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ActionCardBackdrop(accent = accent, modifier = Modifier.matchParentSize())
            YearPill(
                year = year,
                accent = accent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, top = 18.dp, end = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClipboardCardClipart(
                    accent = accent,
                    statusIcon = statusIcon,
                    modifier = Modifier.size(92.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Spacer(Modifier.height(4.dp))
                    TitleWithYear(title = title, year = year, accent = accent, accentDark = accentDark)
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(3.dp)
                            .background(accent.copy(alpha = 0.75f), RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = description,
                        color = Color(0xFF343B4D),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.weight(1f))
                    CardActionBar(
                        label = actionLabel,
                        icon = icon,
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallCardActionBar(
    label: String,
    icon: ImageVector,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = accent,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 7.dp, end = 5.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(19.dp),
        )
    }
}
@Composable
private fun CompactListActionCard(
    title: String,
    year: String,
    description: String,
    actionLabel: String,
    accent: Color,
    accentDark: Color,
    icon: ImageVector,
    statusIcon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ActionCardBackdrop(accent = accent, modifier = Modifier.matchParentSize())

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 20.dp, end = 14.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClipboardCardClipart(
                    accent = accent,
                    statusIcon = statusIcon,
                    modifier = Modifier.size(104.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        YearPill(year = year, accent = accent)
                    }
                    Spacer(Modifier.height(4.dp))
                    TitleWithYear(title = title, year = year, accent = accent, accentDark = accentDark)
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(3.dp)
                            .background(accent.copy(alpha = 0.75f), RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = description,
                        color = Color(0xFF343B4D),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.weight(1f))
                    CardActionBar(
                        label = actionLabel,
                        icon = icon,
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearPill(
    year: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = accent,
        shadowElevation = 4.dp,
    ) {
        Text(
            text = year,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TitleWithYear(
    title: String,
    year: String,
    accent: Color,
    accentDark: Color,
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = title,
            color = HomePurpleDark,
            fontSize = 24.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = year,
            color = accentDark,
            fontSize = 22.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun CardActionBar(
    label: String,
    icon: ImageVector,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(start = 13.dp, end = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(23.dp),
        )
        Text(
            text = label,
            color = accent,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 10.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ClipboardCardClipart(
    accent: Color,
    statusIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = accent.copy(alpha = 0.10f),
                radius = size.minDimension * 0.47f,
                center = Offset(size.width * 0.48f, size.height * 0.50f),
            )
            drawCircle(
                color = accent.copy(alpha = 0.12f),
                radius = 6f,
                center = Offset(size.width * 0.12f, size.height * 0.80f),
            )
            drawCircle(
                color = accent.copy(alpha = 0.10f),
                radius = 5f,
                center = Offset(size.width * 0.88f, size.height * 0.18f),
            )
        }
        Box(
            modifier = Modifier
                .size(70.dp, 88.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(7f, 18f),
                    size = androidx.compose.ui.geometry.Size(size.width - 14f, size.height - 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(40.dp, 22.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.80f), accent))),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 17.dp, top = 37.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.92f)),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp, top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent.copy(alpha = 0.18f)),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width((42 - index * 7).dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent.copy(alpha = 0.13f)),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 7.dp, bottom = 10.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.78f), accent))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun ActionCardBackdrop(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = accent.copy(alpha = 0.045f),
            radius = size.minDimension * 0.58f,
            center = Offset(size.width * 0.20f, size.height * 0.45f),
        )
        val dotColor = accent.copy(alpha = 0.13f)
        drawCircle(color = dotColor, radius = 4.5f, center = Offset(size.width * 0.07f, size.height * 0.76f))
        drawCircle(color = dotColor, radius = 3.2f, center = Offset(size.width * 0.40f, size.height * 0.17f))
        drawCircle(color = dotColor, radius = 2.5f, center = Offset(size.width * 0.42f, size.height * 0.25f))
    }
}
@Composable
private fun DeletedRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(58.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val ribbon = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                cubicTo(size.width * 0.92f, size.height * 0.20f, size.width * 0.72f, size.height * 0.48f, 0f, size.height)
                close()
            }
            drawPath(ribbon, Brush.linearGradient(listOf(Color(0xFFFF5B4F), Color(0xFFD41515))))
        }
        Text(
            text = "NEW",
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 7.dp, top = 17.dp)
                .rotate(-42f),
        )
    }
}

@Composable
private fun DeletedClipart(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(92.dp, 112.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = DeletedRed,
                    topLeft = Offset(5f, 20f),
                    size = androidx.compose.ui.geometry.Size(size.width - 10f, size.height - 25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(54.dp, 24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF7373), DeletedRed))),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 5.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 43.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(DeletedRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("DELETED", color = DeletedRedDark, fontSize = 11.sp, lineHeight = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Text("LIST 2026", color = DeletedRedDark, fontSize = 10.sp, lineHeight = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .width((68 - index * 10).dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEDE8E8)),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFFFF4D4D), DeletedRed))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun DeletedFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFE7E7)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = DeletedRed, modifier = Modifier.size(19.dp))
        }
        Column {
            Text(title, color = Color(0xFF242832), fontSize = 12.sp, lineHeight = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(subtitle, color = Color(0xFF5F6672), fontSize = 10.sp, lineHeight = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun DeletedFeatureDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFFEFE1E1)),
    )
}

@Composable
private fun DeletedCardArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = DeletedRed.copy(alpha = 0.10f),
            radius = size.minDimension * 0.46f,
            center = Offset(size.width * 0.52f, size.height * 0.47f),
        )
        val dotColor = DeletedRed.copy(alpha = 0.16f)
        repeat(4) { row ->
            repeat(3) { column ->
                drawCircle(
                    color = dotColor,
                    radius = 2.4f,
                    center = Offset(size.width * 0.18f + column * 17f, size.height * 0.62f + row * 17f),
                )
            }
        }
        drawCircle(color = DeletedRed.copy(alpha = 0.12f), radius = 5f, center = Offset(size.width * 0.76f, size.height * 0.42f))
    }
}
private val HomePurpleDark = Color(0xFF24106D)
private val HomePurple = Color(0xFF4326B8)

@Composable
private fun VoterListPdfCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopListActionCard(
        title = "Voter List",
        year = "2026",
        description = "Search name and download PDF.",
        actionLabel = "Search & Download",
        accent = VoterBlue,
        accentDark = VoterBlue,
        icon = Icons.Outlined.Download,
        statusIcon = Icons.Outlined.CheckCircle,
        onClick = onClick,
        modifier = modifier,
    )
}
@Composable
private fun VoterListSirCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0B8D45), Color(0xFF087A38)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    OldSirCardDotPattern(modifier = Modifier.matchParentSize())
                    Image(
                        painter = painterResource(R.drawable.voter_list_pdf_clipart),
                        contentDescription = null,
                        modifier = Modifier.size(94.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFFBFEFC))
                        .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE5F5EA),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.voter_list_sir_badge),
                                    color = Green,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5F5EA)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Green,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.voter_list_pdf),
                        color = Color(0xFF064F36),
                        fontSize = 30.sp,
                        lineHeight = 33.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(3.dp)
                            .background(Green, RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.voter_list_sir_desc),
                        color = Color(0xFF202634),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Green.copy(alpha = 0.20f)),
                        )
                        Text(
                            text = stringResource(R.string.voter_list_available_lists),
                            color = Green,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Green.copy(alpha = 0.20f)),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        VoterListTypeChip(
                            label = stringResource(R.string.voter_list_draft),
                            icon = Icons.Outlined.Description,
                            modifier = Modifier.weight(1f),
                        )
                        VoterListTypeChip(
                            label = stringResource(R.string.voter_list_final),
                            icon = Icons.Outlined.CheckCircle,
                            modifier = Modifier.weight(1f),
                        )
                        VoterListTypeChip(
                            label = stringResource(R.string.voter_list_supplementary),
                            icon = Icons.Outlined.FileDownload,
                            modifier = Modifier.weight(1.35f),
                        )
                        VoterListTypeChip(
                            label = stringResource(R.string.voter_list_all_lists),
                            icon = Icons.Outlined.FormatListBulleted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Green)
                    .clickable(onClick = onClick)
                    .padding(start = 104.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.download_pdf),
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun VoterListTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F7F1)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = Green,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
private val OldSirArchiveYears = listOf("2002", "2003", "2004", "2005", "2006")

@Composable
private fun OldSirListCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WideListActionCard(
        title = "Old SIR List",
        year = "2026",
        description = "View and download the Old SIR voter list used in previous enumeration.",
        actionLabel = "View & Download Old SIR List",
        accent = Green,
        accentDark = Color(0xFF168548),
        icon = Icons.Outlined.Download,
        statusIcon = Icons.Outlined.Search,
        onClick = onClick,
        modifier = modifier,
    )
}
@Composable
private fun OldSirArchiveCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(268.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0B8D45), Color(0xFF087A38)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                OldSirCardDotPattern(modifier = Modifier.matchParentSize())
                Image(
                    painter = painterResource(R.drawable.old_sir_pdf_clipart),
                    contentDescription = null,
                    modifier = Modifier.size(102.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE5F5EA),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Green,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.old_sir_archive_badge),
                                color = Green,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5F5EA)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }

                Spacer(Modifier.height(7.dp))
                Text(
                    text = stringResource(R.string.old_sir_archive_title),
                    color = Color(0xFF064F36),
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.old_sir_archive_desc),
                    color = Color(0xFF202634),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Green.copy(alpha = 0.14f)),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OldSirArchiveYears.forEach { year ->
                        OldSirYearChip(
                            year = year,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(9.dp))
                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Green,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 18.dp, end = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = stringResource(R.string.old_sir_archive_action),
                            color = Color.White,
                            fontSize = 18.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = stringResource(R.string.old_sir_archive_verified),
                        color = Color(0xFF6D7179),
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun OldSirYearChip(
    year: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F7F1)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = year,
            color = Color(0xFF064F36),
            fontSize = 14.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun OldSirCardDotPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val dotColor = Color.White.copy(alpha = 0.18f)
        val startX = size.width * 0.18f
        val startY = size.height * 0.68f
        repeat(7) { row ->
            repeat(5) { column ->
                drawCircle(
                    color = dotColor,
                    radius = 2.1f,
                    center = Offset(startX + column * 13f, startY + row * 12f),
                )
            }
        }
    }
}
@Composable
private fun HomePdfCard(
    title: String,
    description: String,
    actionLabel: String,
    showTrending: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GreenContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0C8238), Color(0xFF128D41)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5F5EA))
                        .padding(start = 18.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = title,
                            color = Green,
                            fontSize = 21.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (showTrending) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Green.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                    contentDescription = "Trending",
                                    tint = Green,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(3.dp)
                            .background(Green),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = description,
                        color = Green,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(Green)
                        .clickable(onClick = onClick)
                        .padding(start = 22.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = actionLabel,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureReliableCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF6544C6),
                    modifier = Modifier.size(42.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "100% Secure & Reliable",
                    color = Color(0xFF4F3BB5),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "All data is fetched from official sources and provided for reference only.",
                    color = Color(0xFF777286),
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6544C6),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun LegalContactCard() {
    val uriHandler = LocalUriHandler.current
    val privacyUrl = "https://sites.google.com/view/voterlist2026/home"
    val instagramUrl = "https://www.instagram.com/samoon_digital/"
    val email = "alimailsamun@gmail.com"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Disclaimer",
                    color = Green,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "This is an unofficial app and is not affiliated with or authorized by any government or government authority.",
                    color = Color(0xFF777286),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            LegalContactRow(
                label = "Privacy Policy",
                onClick = { uriHandler.openUri(privacyUrl) },
            )
            LegalContactRow(
                label = "Follow us",
                onClick = { uriHandler.openUri(instagramUrl) },
            )
            LegalContactRow(
                label = "Email us",
                onClick = { uriHandler.openUri("mailto:$email") },
            )
        }
    }
}

@Composable
private fun LegalContactRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (label == "Email us") Icons.Outlined.Email else Icons.Outlined.Link,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(16.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = label,
                color = Color(0xFF4F3BB5),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF6544C6),
            modifier = Modifier.size(20.dp),
        )
    }
}
