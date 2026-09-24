package com.samoondigital.yojnaplus.core.ui.components

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AdUnitIds
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

@Composable
fun AdMobBannerAd(
    placementKey: String,
    modifier: Modifier = Modifier,
    onHeightChanged: (Dp) -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val adWidth = maxWidth.value.roundToInt().coerceAtLeast(1)
        val adSize = remember(context, adWidth) { largeAnchoredAdaptiveBannerSize(context, adWidth) }
        val adSizes = remember(context, adWidth) { bannerAdSizes(context, adWidth) }
        val requestedHeight = adSize.height.dp
        val adUnitId = AdUnitIds.banner
        var bannerWidth by remember(placementKey, adWidth, adUnitId) { mutableStateOf(maxWidth) }
        var bannerHeight by remember(placementKey, adWidth, adUnitId) { mutableStateOf(requestedHeight) }
        val adView = remember(placementKey, adWidth, adUnitId) {
            AdView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                resize(adSize)
            }
        }

        LaunchedEffect(bannerHeight) {
            onHeightChanged(bannerHeight)
        }

        DisposableEffect(adView) {
            val active = AtomicBoolean(true)
            val cancelPendingLoad = AdManager.loadWhenReady(
                format = "banner",
                adUnitId = adUnitId,
                isActive = { active.get() },
            ) {
                if (active.get()) {
                    adView.loadAd(
                        BannerAdRequest.Builder(adUnitId, adSizes).build(),
                        object : AdLoadCallback<BannerAd> {
                            override fun onAdLoaded(ad: BannerAd) {
                                AdManager.onAdLoaded("banner", adUnitId, ad.getResponseInfo())
                                val loadedAdSize = ad.getAdSize()
                                adView.post {
                                    if (active.get()) {
                                        bannerWidth = loadedAdSize.widthDp(context, density, maxWidth)
                                        bannerHeight = loadedAdSize.heightDp(context, density, requestedHeight)
                                        adView.resize(loadedAdSize)
                                        Log.d(
                                            "AdMob",
                                            "loaded-size format=banner placement=$placementKey width=${loadedAdSize.width} height=${loadedAdSize.height}",
                                        )
                                    }
                                }
                                ad.adEventCallback = object : BannerAdEventCallback {
                                    override fun onAdImpression() {
                                        Log.d("AdMob", "impression format=banner placement=$placementKey unit=$adUnitId")
                                    }

                                    override fun onAdClicked() {
                                        Log.d("AdMob", "clicked format=banner placement=$placementKey unit=$adUnitId")
                                    }
                                }
                                ad.bannerAdRefreshCallback = object : BannerAdRefreshCallback {
                                    override fun onAdFailedToRefresh(adError: LoadAdError) {
                                        AdManager.onAdFailed("banner-refresh", adUnitId, adError)
                                    }
                                }
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                AdManager.onAdFailed("banner", adUnitId, adError)
                            }
                        },
                    )
                }
            }

            onDispose {
                active.set(false)
                cancelPendingLoad()
                adView.destroy()
            }
        }

        AndroidView(
            factory = { adView },
            modifier = Modifier
                .width(bannerWidth)
                .height(bannerHeight),
            update = { it.resize(adView.getBannerAd()?.getAdSize() ?: adSize) },
        )
    }
}

@Composable
fun rememberLargeAdaptiveBannerHeight(adWidth: Dp): Dp {
    val context = LocalContext.current
    val widthDp = adWidth.value.roundToInt().coerceAtLeast(1)
    return remember(context, widthDp) {
        largeAnchoredAdaptiveBannerSize(context, widthDp).height.dp
    }
}

private fun largeAnchoredAdaptiveBannerSize(
    context: android.content.Context,
    adWidth: Int,
): AdSize {
    return AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, adWidth)
}

@Suppress("DEPRECATION")
private fun bannerAdSizes(
    context: android.content.Context,
    adWidth: Int,
): List<AdSize> {
    val standardAdaptive = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    val largeAdaptive = largeAnchoredAdaptiveBannerSize(context, adWidth)
    return buildList {
        if (adWidth >= AdSize.BANNER_WIDTH) add(AdSize.BANNER)
        add(standardAdaptive)
        add(largeAdaptive)
    }.distinct()
}

private fun AdSize.widthDp(
    context: android.content.Context,
    density: Density,
    fallback: Dp,
): Dp {
    val widthPx = getWidthInPixels(context)
    if (widthPx <= 0) return fallback
    val loadedWidth = with(density) { widthPx.toDp() }
    return if (loadedWidth > fallback) fallback else loadedWidth
}

private fun AdSize.heightDp(
    context: android.content.Context,
    density: Density,
    fallback: Dp,
): Dp {
    val heightPx = getHeightInPixels(context)
    if (heightPx <= 0) return fallback
    return with(density) { heightPx.toDp() }
}
