package com.samoondigital.yojnaplus.core.ui.components

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
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
        val adUnitId = AdUnitIds.banner
        val adView = remember(placementKey, adWidth, adUnitId) {
            AdView(context).apply {
                setAdUnitId(adUnitId)
                setAdSize(anchoredAdaptiveBannerSize(context, adWidth))
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        AdManager.onAdLoaded("banner", adUnitId, responseInfo)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        AdManager.onAdFailed("banner", adUnitId, error)
                    }

                    override fun onAdImpression() {
                        Log.d("AdMob", "impression format=banner placement=$placementKey unit=$adUnitId")
                    }

                    override fun onAdClicked() {
                        Log.d("AdMob", "clicked format=banner placement=$placementKey unit=$adUnitId")
                    }
                }
            }
        }

        DisposableEffect(adView) {
            val active = AtomicBoolean(true)
            val cancelPendingLoad = AdManager.loadWhenReady(
                format = "banner",
                adUnitId = adUnitId,
                isActive = { active.get() },
            ) { request ->
                if (active.get()) adView.loadAd(request)
            }

            onDispose {
                active.set(false)
                cancelPendingLoad()
                adView.destroy()
            }
        }

        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Suppress("DEPRECATION")
private fun anchoredAdaptiveBannerSize(
    context: android.content.Context,
    adWidth: Int,
): AdSize {
    // Keep the production-tested banner height behavior; the non-deprecated large banner API changes sizing.
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}
