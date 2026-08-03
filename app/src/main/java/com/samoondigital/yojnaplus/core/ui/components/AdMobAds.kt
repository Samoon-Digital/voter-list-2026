package com.samoondigital.yojnaplus.core.ui.components

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AdUnitIds
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

private val NativeAdHeight = 320.dp
private const val NativeAdMediaHeightDp = 132
private const val NativeAdMaxRetryCount = 2
private const val NativeAdRetryDelayMillis = 1_500L

@Composable
fun LazyNativeAdItem(
    listState: LazyListState,
    itemKey: Any,
    placementKey: String,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember(itemKey) { mutableStateOf(false) }
    LaunchedEffect(listState, itemKey) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.key == itemKey } }
            .collect { visible -> if (visible) isVisible = true }
    }
    AdMobNativeAd(
        placementKey = placementKey,
        enabled = isVisible,
        modifier = modifier,
    )
}

@Composable
fun AdMobNativeAd(
    modifier: Modifier = Modifier,
    placementKey: String = "native-default",
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val adUnitId = AdUnitIds.native
    var nativeAd by remember(placementKey) { mutableStateOf<NativeAd?>(null) }
    var retryAttempt by remember(placementKey) { mutableStateOf(0) }
    var retryNonce by remember(placementKey) { mutableStateOf(0) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(context, placementKey, enabled, retryNonce) {
        val disposed = AtomicBoolean(false)
        val retryRunnable = Runnable {
            if (!disposed.get()) retryNonce += 1
        }
        if (!enabled) {
            onDispose {
                disposed.set(true)
                mainHandler.removeCallbacks(retryRunnable)
            }
        } else {
            val loader = NativeAdLoader(
                context = context,
                adUnitId = adUnitId,
                placementKey = placementKey,
                isActive = { !disposed.get() },
                onLoaded = { loadedAd ->
                    retryAttempt = 0
                    nativeAd?.destroy()
                    nativeAd = loadedAd
                },
                onFailed = {
                    if (retryAttempt < NativeAdMaxRetryCount && !disposed.get()) {
                        retryAttempt += 1
                        mainHandler.postDelayed(
                            retryRunnable,
                            NativeAdRetryDelayMillis * retryAttempt,
                        )
                    }
                },
            )
            val cancelLoad = loader.load()
            onDispose {
                disposed.set(true)
                mainHandler.removeCallbacks(retryRunnable)
                cancelLoad()
                nativeAd?.destroy()
                nativeAd = null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeAdHeight),
    ) {
        if (nativeAd == null) {
            NativeAdSkeleton(modifier = Modifier.fillMaxSize())
        }
        nativeAd?.let { ad ->
            AndroidView(
                factory = { createNativeAdView(it).also { view -> view.bind(ad) } },
                update = { it.bind(ad) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun NativeAdSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "native-ad-shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -360f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "native-ad-shimmer-x",
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE4EEF8),
            Color(0xFFF9FCFF),
            Color(0xFFE4EEF8),
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 260f, 220f),
    )
    val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE3E2F5), cardShape)
            .padding(10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NativeAdMediaHeightDp.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush),
            ) {
                Text(
                    text = "Ad",
                    color = Color(0xFF607080),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.86f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                ) {
                    val triangle = Path().apply {
                        moveTo(size.width * 0.42f, size.height * 0.32f)
                        lineTo(size.width * 0.42f, size.height * 0.68f)
                        lineTo(size.width * 0.70f, size.height * 0.50f)
                        close()
                    }
                    drawPath(triangle, Color(0xFF116DDC))
                }
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                ) {
                    drawCircle(
                        color = Color(0xFF7B8794),
                        radius = size.minDimension * 0.34f,
                        style = Stroke(width = 1.5f),
                    )
                    drawCircle(
                        color = Color(0xFF7B8794),
                        radius = 1.2f,
                        center = Offset(size.width / 2f, size.height * 0.34f),
                    )
                    drawLine(
                        color = Color(0xFF7B8794),
                        start = Offset(size.width / 2f, size.height * 0.46f),
                        end = Offset(size.width / 2f, size.height * 0.68f),
                        strokeWidth = 1.5f,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonLine(shimmerBrush, widthFraction = 0.92f, height = 12)
                    SkeletonLine(shimmerBrush, widthFraction = 0.72f, height = 10)
                }
                Text(
                    text = "Sponsored",
                    color = Color(0xFF607080),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFD7E3EF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawStar(Color(0xFFF6C343))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                SkeletonLine(shimmerBrush, widthFraction = 0.24f, height = 8)
            }

            SkeletonLine(shimmerBrush, widthFraction = 1f, height = 11)
            SkeletonLine(shimmerBrush, widthFraction = 0.72f, height = 10)

            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF116DDC)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Learn more",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SkeletonLine(
    brush: Brush,
    widthFraction: Float,
    height: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(color: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.minDimension / 2f
    val innerRadius = outerRadius * 0.48f
    val path = Path()
    repeat(10) { index ->
        val radius = if (index % 2 == 0) outerRadius else innerRadius
        val angle = Math.toRadians((index * 36 - 90).toDouble())
        val point = Offset(
            x = center.x + (kotlin.math.cos(angle) * radius).toFloat(),
            y = center.y + (kotlin.math.sin(angle) * radius).toFloat(),
        )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, color)
}

private class NativeAdLoader(
    private val context: android.content.Context,
    private val adUnitId: String,
    private val placementKey: String,
    private val isActive: () -> Boolean,
    private val onLoaded: (NativeAd) -> Unit,
    private val onFailed: () -> Unit,
) {
    fun load(): () -> Unit {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { loadedAd ->
                if (isActive()) {
                    onLoaded(loadedAd)
                    AdManager.onAdLoaded("native", adUnitId, loadedAd.responseInfo)
                } else {
                    loadedAd.destroy()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    AdManager.onAdFailed("native", adUnitId, error)
                    if (isActive()) onFailed()
                }

                override fun onAdImpression() {
                    android.util.Log.d("AdMob", "impression format=native placement=$placementKey unit=$adUnitId")
                }

                override fun onAdClicked() {
                    android.util.Log.d("AdMob", "clicked format=native placement=$placementKey unit=$adUnitId")
                }
            })
            .build()
        return AdManager.loadWhenReady(
            format = "native",
            adUnitId = adUnitId,
            isActive = isActive,
            load = { request: AdRequest -> adLoader.loadAd(request) },
        )
    }
}

private fun createNativeAdView(context: android.content.Context): NativeAdView {
    val density = context.resources.displayMetrics.density
    fun dp(value: Int) = (value * density).roundToInt()

    val root = NativeAdView(context).apply {
        background = GradientDrawable().apply {
            setColor(0xFFFFFFFF.toInt())
            cornerRadius = dp(8).toFloat()
            setStroke(dp(1), 0xFFE3E2F5.toInt())
        }
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }
    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
    val mediaFrame = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(NativeAdMediaHeightDp),
        )
        background = GradientDrawable().apply {
            setColor(0xFFE7F0FA.toInt())
            cornerRadius = dp(6).toFloat()
        }
    }
    val media = MediaView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
    val attribution = TextView(context).apply {
        text = context.getString(R.string.ad_label)
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(0xFF4A4A4A.toInt())
        setPadding(dp(5), dp(2), dp(5), dp(2))
        background = GradientDrawable().apply {
            setColor(0xE6FFFFFF.toInt())
            cornerRadius = dp(3).toFloat()
        }
        isClickable = false
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START,
        ).apply {
            marginStart = dp(7)
            topMargin = dp(7)
        }
    }
    val choices = AdChoicesView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END,
        ).apply {
            marginEnd = dp(6)
            topMargin = dp(6)
        }
    }
    val infoRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(9), 0, 0)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    val icon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = GradientDrawable().apply {
            setColor(0xFFEAF2F8.toInt())
            cornerRadius = dp(6).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginEnd = dp(10) }
    }
    val textColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }
    val headline = TextView(context).apply {
        setTextColor(0xFF090B1F.toInt())
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 1
        includeFontPadding = false
    }
    val body = TextView(context).apply {
        setTextColor(0xFF686A8D.toInt())
        textSize = 12f
        maxLines = 2
        includeFontPadding = false
    }
    val advertiser = TextView(context).apply {
        setTextColor(0xFF607080.toInt())
        textSize = 9f
        setPadding(dp(7), dp(2), dp(7), dp(2))
        background = GradientDrawable().apply {
            setColor(0xFFFFFFFF.toInt())
            cornerRadius = dp(10).toFloat()
            setStroke(dp(1), 0xFFD7E3EF.toInt())
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { marginStart = dp(8) }
    }
    val rating = RatingBar(context, null, android.R.attr.ratingBarStyleSmall).apply {
        setNumStars(5)
        setStepSize(0.5f)
        setIsIndicator(true)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(24),
        ).apply { topMargin = dp(5) }
    }
    val callToAction = Button(context).apply {
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        minHeight = 0
        minWidth = 0
        includeFontPadding = false
        setTextColor(0xFFFFFFFF.toInt())
        setPadding(dp(10), 0, dp(10), 0)
        background = GradientDrawable().apply {
            setColor(0xFF116DDC.toInt())
            cornerRadius = dp(6).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(38),
        ).apply { topMargin = dp(8) }
    }

    mediaFrame.addView(media)
    mediaFrame.addView(attribution)
    mediaFrame.addView(choices)
    textColumn.addView(headline)
    textColumn.addView(body)
    infoRow.addView(icon)
    infoRow.addView(textColumn)
    infoRow.addView(advertiser)
    column.addView(mediaFrame)
    column.addView(infoRow)
    column.addView(rating)
    column.addView(callToAction)
    root.addView(column)
    root.mediaView = media
    root.adChoicesView = choices
    root.iconView = icon
    root.headlineView = headline
    root.bodyView = body
    root.advertiserView = advertiser
    root.starRatingView = rating
    root.callToActionView = callToAction
    return root
}

private fun NativeAdView.bind(ad: NativeAd) {
    mediaView?.mediaContent = ad.mediaContent
    (headlineView as TextView).text = ad.headline
    (bodyView as TextView).apply {
        text = ad.body.orEmpty()
        visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (advertiserView as TextView).apply {
        text = ad.advertiser ?: "Sponsored"
        visibility = View.VISIBLE
    }
    (starRatingView as RatingBar).apply {
        val ratingValue = ad.starRating?.toFloat()
        this.rating = ratingValue ?: 0f
        visibility = if (ratingValue == null) View.GONE else View.VISIBLE
    }
    (callToActionView as Button).apply {
        text = ad.callToAction.orEmpty()
        visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (iconView as ImageView).apply {
        setImageDrawable(ad.icon?.drawable)
        visibility = if (ad.icon == null) View.GONE else View.VISIBLE
    }
    setNativeAd(ad)
}
