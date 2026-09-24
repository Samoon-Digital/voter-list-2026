package com.samoondigital.yojnaplus.core.ui.components

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val adWidth = maxWidth.value.roundToInt().coerceAtLeast(1)
        val adSize = remember(context, adWidth) { largeAnchoredAdaptiveBannerSize(context, adWidth) }
        val bannerHeight = adSize.height.dp
        val adUnitId = AdUnitIds.banner
        val adView = remember(placementKey, adWidth, adUnitId) {
            AdView(context).apply {
                resize(adSize)
            }
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
                        BannerAdRequest.Builder(adUnitId, adSize).build(),
                        object : AdLoadCallback<BannerAd> {
                            override fun onAdLoaded(ad: BannerAd) {
                                AdManager.onAdLoaded("banner", adUnitId, ad.getResponseInfo())
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
                .fillMaxWidth()
                .height(bannerHeight),
            update = { it.resize(adSize) },
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
