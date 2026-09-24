package com.samoondigital.yojnaplus.feature.splash

import android.app.Activity
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.samoondigital.yojnaplus.R

@Composable
fun StartupSplashScreen(
    versionName: String,
    startedAtMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    SplashSystemBarEffect()

    val elapsedMs = remember(startedAtMs) {
        (SystemClock.elapsedRealtime() - startedAtMs).coerceAtLeast(0L)
    }
    val initialProgress = (elapsedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    val remainingMs = (durationMs - elapsedMs).coerceAtLeast(0L)
    val progress = remember(startedAtMs) { Animatable(initialProgress) }

    LaunchedEffect(startedAtMs, durationMs) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = remainingMs.toInt().coerceAtLeast(1),
                easing = LinearEasing,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        SplashAmbientBackground(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.52f))
            SplashBrandMark()
            Spacer(Modifier.height(22.dp))
            OfficialEditionPill()
            Spacer(Modifier.height(12.dp))
            SplashTitle()
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Electoral Roll & Voter Services",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(0.62f))
            LoadingSection(progress = progress.value)
            Spacer(Modifier.height(26.dp))
            VerificationFooter(versionName = versionName)
            Spacer(Modifier.height(26.dp))
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE2E8F0)),
            )
        }
    }
}

@Composable
private fun SplashAmbientBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.43f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEFF6FF), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.75f,
            ),
            radius = size.minDimension * 0.72f,
            center = center,
        )
        drawCircle(
            color = Color(0xFFDBEAFE).copy(alpha = 0.42f),
            radius = size.minDimension * 0.36f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f),
        )
        drawCircle(
            color = Color(0xFFEFF6FF).copy(alpha = 0.72f),
            radius = size.minDimension * 0.54f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.0f),
        )
        drawCircle(
            color = Color(0xFFF1F5F9).copy(alpha = 0.80f),
            radius = size.minDimension * 0.74f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.0f),
        )
    }
}

@Composable
private fun SplashBrandMark() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(142.dp)
                .clip(CircleShape)
                .background(Color(0xFFDBEAFE).copy(alpha = 0.50f)),
        )
        Surface(
            shape = CircleShape,
            color = Color(0xFF2D31E8),
            shadowElevation = 12.dp,
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier.size(130.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
            )
        }
    }
}

@Composable
private fun OfficialEditionPill() {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
            )
            Text(
                text = "OFFICIAL EDITION 2026",
                color = Color(0xFF1D4ED8),
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SplashTitle() {
    Text(
        text = buildAnnotatedString {
            append("Voter List ")
            withStyle(SpanStyle(color = Color(0xFF1D4ED8))) {
                append("2026")
            }
        },
        color = Color(0xFF0F172A),
        fontSize = 24.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun LoadingSection(progress: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(210.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E7EB)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF2563EB)),
            )
        }
        Text(
            text = "Preparing portal data...",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun VerificationFooter(versionName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.VerifiedUser,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Secure & Verified Data",
            color = Color(0xFF475569),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFCBD5E1)),
        )
        Text(
            text = "Version $versionName",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SplashSystemBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window == null || view.isInEditMode) {
            onDispose { }
        } else {
            @Suppress("DEPRECATION")
            val previousStatusColor = window.statusBarColor
            @Suppress("DEPRECATION")
            val previousNavigationColor = window.navigationBarColor
            val controller = WindowInsetsControllerCompat(window, view)
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            val previousLightNavigationBars = controller.isAppearanceLightNavigationBars

            @Suppress("DEPRECATION")
            window.statusBarColor = Color.White.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.White.toArgb()
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true

            onDispose {
                @Suppress("DEPRECATION")
                window.statusBarColor = previousStatusColor
                @Suppress("DEPRECATION")
                window.navigationBarColor = previousNavigationColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
                controller.isAppearanceLightNavigationBars = previousLightNavigationBars
            }
        }
    }
}
