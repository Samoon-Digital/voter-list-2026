package com.samoondigital.yojnaplus.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.theme.Green
import com.samoondigital.yojnaplus.core.ui.theme.GreenContainer

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
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        HomeHeader(
            onOpenDownloads = onOpenDownloads,
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(24.dp))
            VoterListPdfCard(onClick = onDownloadPdf)
            Spacer(Modifier.height(28.dp))
            SecureReliableCard()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onOpenDownloads: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF25108B), Color(0xFF5132C4)),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.width * 0.1f,
                center = Offset(size.width * 0.52f, size.height * 0.2f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.08f,
                center = Offset(size.width * 0.25f, size.height * 0.1f),
            )

            val dotRadius = 1.5.dp.toPx()
            val startX = size.width - 42.dp.toPx()
            val startY = 78.dp.toPx()
            repeat(5) { row ->
                repeat(6) { column ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = dotRadius,
                        center = Offset(
                            x = startX + column * 7.dp.toPx(),
                            y = startY + row * 7.dp.toPx(),
                        ),
                    )
                }
            }

            val waveTop = size.height - 23.dp.toPx()
            val path = Path().apply {
                moveTo(0f, waveTop)
                cubicTo(
                    size.width * 0.2f,
                    waveTop - 9.dp.toPx(),
                    size.width * 0.32f,
                    waveTop + 10.dp.toPx(),
                    size.width * 0.48f,
                    waveTop,
                )
                cubicTo(
                    size.width * 0.64f,
                    waveTop - 11.dp.toPx(),
                    size.width * 0.7f,
                    waveTop + 8.dp.toPx(),
                    size.width,
                    waveTop - 1.dp.toPx(),
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenDownloads)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Downloads",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "Downloads",
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun VoterListPdfCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GreenContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        onClick = onClick,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(96.dp)
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
                    modifier = Modifier.size(58.dp),
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE5F5EA))
                        .padding(start = 28.dp, top = 34.dp, end = 22.dp),
                ) {
                    Text(
                        text = stringResource(R.string.voter_list_pdf),
                        color = Green,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(4.dp)
                            .background(Green),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.voter_list_pdf_desc),
                        color = Green,
                        fontSize = 18.sp,
                        lineHeight = 29.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Green)
                        .clickable(onClick = onClick)
                        .padding(start = 36.dp, end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.download_pdf),
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
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
            ) {
                Text(
                    text = "100% Secure & Reliable",
                    color = Color(0xFF4F3BB5),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "All data is fetched from official sources\nand provided for reference only.",
                    color = Color(0xFF777286),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6544C6),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
