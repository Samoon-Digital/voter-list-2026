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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AdUnitIds

@Composable
fun AdMobBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val width = (LocalConfiguration.current.screenWidthDp - 32).coerceAtLeast(320)
    var loaded by remember { mutableStateOf(false) }
    val adSize = remember(context, width) { AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width) }
    val adView = remember(context, adSize) {
        AdView(context).apply {
            adUnitId = AdUnitIds.Banner
            setAdSize(adSize)
            adListener = object : AdListener() {
                override fun onAdLoaded() { loaded = true }
                override fun onAdFailedToLoad(error: LoadAdError) { loaded = false }
                override fun onAdImpression() { android.util.Log.d("AdManager", "Banner impression") }
                override fun onAdClicked() { android.util.Log.d("AdManager", "Banner clicked") }
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }
    AnimatedVisibility(visible = loaded, modifier = modifier) {
        AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth().wrapContentHeight())
    }
}

@Composable
fun AdMobNativeAd(modifier: Modifier = Modifier) {
    val signal by AdManager.nativeSignal.collectAsState()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    LaunchedEffect(signal) { if (nativeAd == null) nativeAd = AdManager.takeNativeAd() }
    DisposableEffect(nativeAd) {
        val owned = nativeAd
        onDispose { owned?.destroy() }
    }
    AnimatedVisibility(visible = nativeAd != null, modifier = modifier) {
        nativeAd?.let { ad ->
            AndroidView(
                factory = { createNativeAdView(it) },
                update = { it.bind(ad) },
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            )
        }
    }
}

private fun createNativeAdView(context: android.content.Context): NativeAdView {
    val d = context.resources.displayMetrics.density
    fun dp(v: Int) = (v * d).toInt()
    val root = NativeAdView(context).apply {
        background = GradientDrawable().apply {
            setColor(0xFFFFFFFF.toInt())
            cornerRadius = dp(8).toFloat()
            setStroke(dp(1), 0xFFE3E2F5.toInt())
        }
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }
    val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val mediaFrame = FrameLayout(context)
    val media = MediaView(context).apply { layoutParams = FrameLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT) }
    val choices = AdChoicesView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END)
    }
    val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, 0) }
    val icon = ImageView(context).apply { layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(10) } }
    val textCol = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
    val headline = TextView(context).apply { setTextColor(0xFF090B1F.toInt()); textSize = 15f; typeface = Typeface.DEFAULT_BOLD; maxLines = 1 }
    val body = TextView(context).apply { setTextColor(0xFF686A8D.toInt()); textSize = 12f; maxLines = 2 }
    val cta = Button(context).apply { textSize = 12f; isAllCaps = false }
    mediaFrame.addView(media)
    mediaFrame.addView(choices)
    textCol.addView(headline)
    textCol.addView(body)
    row.addView(icon)
    row.addView(textCol)
    row.addView(cta)
    col.addView(mediaFrame)
    col.addView(row)
    root.addView(col)
    root.mediaView = media
    root.adChoicesView = choices
    root.iconView = icon
    root.headlineView = headline
    root.bodyView = body
    root.callToActionView = cta
    return root
}

private fun NativeAdView.bind(ad: NativeAd) {
    mediaView?.mediaContent = ad.mediaContent
    (headlineView as TextView).text = ad.headline
    (bodyView as TextView).apply { text = ad.body.orEmpty(); visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE }
    (callToActionView as Button).apply { text = ad.callToAction.orEmpty(); visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE }
    (iconView as ImageView).apply { setImageDrawable(ad.icon?.drawable); visibility = if (ad.icon == null) View.GONE else View.VISIBLE }
    setNativeAd(ad)
}