package com.samoondigital.yojnaplus.core.ui.components

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val disposed = remember(placementKey) { AtomicBoolean(false) }

    DisposableEffect(context, placementKey, enabled) {
        disposed.set(false)
        if (!enabled) {
            onDispose { disposed.set(true) }
        } else {
            val loader = NativeAdLoader(
                context = context,
                adUnitId = adUnitId,
                placementKey = placementKey,
                isActive = { !disposed.get() },
                onLoaded = { loadedAd ->
                    nativeAd?.destroy()
                    nativeAd = loadedAd
                },
            )
            val cancelLoad = loader.load()
            onDispose {
                disposed.set(true)
                cancelLoad()
                nativeAd?.destroy()
                nativeAd = null
            }
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            factory = { createNativeAdView(it).also { view -> view.bind(ad) } },
            update = { it.bind(ad) },
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        )
    }
}

private class NativeAdLoader(
    private val context: android.content.Context,
    private val adUnitId: String,
    private val placementKey: String,
    private val isActive: () -> Boolean,
    private val onLoaded: (NativeAd) -> Unit,
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
    val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val mediaFrame = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(160))
    }
    val media = MediaView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val attribution = TextView(context).apply {
        text = context.getString(R.string.ad_label)
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(0xFF4A4A4A.toInt())
        setPadding(dp(4), dp(2), dp(4), dp(2))
        background = GradientDrawable().apply {
            setColor(0xFFF0F0F0.toInt())
            cornerRadius = dp(3).toFloat()
        }
        isClickable = false
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            marginStart = dp(6)
            topMargin = dp(6)
        }
    }
    val choices = AdChoicesView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END)
    }
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, 0)
    }
    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(10) }
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
    }
    val body = TextView(context).apply {
        setTextColor(0xFF686A8D.toInt())
        textSize = 12f
        maxLines = 2
    }
    val callToAction = Button(context).apply {
        textSize = 12f
        isAllCaps = false
    }

    mediaFrame.addView(media)
    mediaFrame.addView(attribution)
    mediaFrame.addView(choices)
    textColumn.addView(headline)
    textColumn.addView(body)
    row.addView(icon)
    row.addView(textColumn)
    row.addView(callToAction)
    column.addView(mediaFrame)
    column.addView(row)
    root.addView(column)
    root.mediaView = media
    root.adChoicesView = choices
    root.iconView = icon
    root.headlineView = headline
    root.bodyView = body
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
