package com.samoondigital.yojnaplus.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.samoondigital.yojnaplus.R

private const val MaxBannerHeightDp = 90

/** Owned by the selection route, so scrolling or changing steps does not reload the ad. */
@Composable
internal fun rememberInlineBanner(widthDp: Int): InlineBannerState {
    val context = LocalContext.current
    val state = remember(context, widthDp) { InlineBannerState(context, widthDp) }
    DisposableEffect(state) {
        state.load()
        onDispose { state.destroy() }
    }
    return state
}

internal class InlineBannerState(private val context: Context, private val widthDp: Int) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var active = true
    private var cancelPending: (() -> Unit)? = null
    var view: AdView? by mutableStateOf(null)
        private set
    var available by mutableStateOf(!AdManager.areAdsTemporarilyDisabled() && widthDp > 0)
        private set

    fun load() {
        if (!available || view != null) return
        val adView = AdView(context)
        view = adView
        cancelPending = AdManager.loadWhenReady("banner", AdUnitIds.banner, { active }) {
            try {
                val size = AdSize.getInlineAdaptiveBannerAdSize(widthDp, MaxBannerHeightDp)
                adView.loadAd(
                    BannerAdRequest.Builder(AdUnitIds.banner, size).build(),
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            mainHandler.post {
                                if (active) AdManager.onAdLoaded("banner", AdUnitIds.banner, ad.getResponseInfo())
                                else ad.destroy()
                            }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            mainHandler.post {
                                if (active) {
                                    available = false
                                    AdManager.onAdFailed("banner", AdUnitIds.banner, adError)
                                }
                            }
                        }
                    },
                )
            } catch (error: Exception) {
                available = false
                Log.e("AdMob", "Inline banner could not load", error)
            }
        }
    }

    fun destroy() {
        active = false
        cancelPending?.invoke()
        cancelPending = null
        view?.let { adView ->
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.destroy()
        }
        view = null
    }
}

@Composable
internal fun InlineBanner(state: InlineBannerState) {
    if (!state.available) return
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp).testTag("inline-banner"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.ui_advertisement),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        // Reserve the maximum requested height before loading to keep list rows stable.
        // Only the route owns/destroys the AdView; lazy hosts just attach and detach it.
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(MaxBannerHeightDp.dp),
            factory = { FrameLayout(it) },
            onReset = null,
            onRelease = { host -> host.removeAllViews() },
            update = { host ->
                state.view?.let { adView ->
                    if (adView.parent !== host) {
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        host.addView(adView, FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER,
                        ))
                    }
                }
            },
        )
    }
}
