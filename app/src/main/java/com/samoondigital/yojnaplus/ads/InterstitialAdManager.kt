package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

object InterstitialAdManager {
    private const val Tag = "AdMobInterstitial"
    private const val Format = "interstitial"
    private val mainHandler = Handler(Looper.getMainLooper())

    private var appContext: Context? = null
    private var interstitialAd: InterstitialAd? = null
    private var loading = false
    private var retryAttempt = 0
    private var retryRunnable: Runnable? = null
    private val frequencyGate = InterstitialFrequencyGate()

    fun preload(context: Context) {
        appContext = context.applicationContext
        mainHandler.post { preloadOnMain() }
    }

    fun showIfAvailable(
        activity: Activity?,
        placement: String = "default",
        isRequestActive: () -> Boolean = { true },
        onContinue: () -> Unit,
    ) {
        mainHandler.post {
            if (!isRequestActive()) return@post
            val lifecycle = (activity as? LifecycleOwner)?.lifecycle
            if (activity == null || activity.isFinishing || activity.isDestroyed ||
                lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false
            ) {
                Log.d(Tag, "show-skipped placement=$placement reason=activity-inactive")
                onContinue()
                return@post
            }
            val ad = interstitialAd
            if (ad == null) {
                Log.d(Tag, "show-skipped placement=$placement reason=not-loaded")
                preload(activity.applicationContext)
                onContinue()
                return@post
            }
            if (!frequencyGate.tryStart(SystemClock.elapsedRealtime())) {
                Log.d(Tag, "show-skipped placement=$placement reason=cooldown-or-showing")
                onContinue()
                return@post
            }

            interstitialAd = null
            AppOpenAdManager.onExternalFullScreenAdWillShow()
            val continued = AtomicBoolean(false)
            fun finishOnce() {
                if (!continued.compareAndSet(false, true)) return
                frequencyGate.finish(SystemClock.elapsedRealtime())
                if (!activity.isDestroyed) activity.restoreDefaultSystemBarLayout()
                AppOpenAdManager.onExternalFullScreenAdFinished()
                preload(activity.applicationContext)
                onContinue()
            }

            ad.adEventCallback = object : InterstitialAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    mainHandler.post {
                        Log.d(Tag, "show-started placement=$placement unit=${AdUnitIds.interstitial}")
                    }
                }

                override fun onAdDismissedFullScreenContent() {
                    mainHandler.post {
                        Log.d(Tag, "show-dismissed placement=$placement unit=${AdUnitIds.interstitial}")
                        finishOnce()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    mainHandler.post {
                        Log.w(
                            Tag,
                            "show-failed placement=$placement unit=${AdUnitIds.interstitial} code=${fullScreenContentError.code} message=${fullScreenContentError.message}",
                        )
                        finishOnce()
                    }
                }

                override fun onAdImpression() {
                    Log.d(Tag, "impression format=interstitial placement=$placement unit=${AdUnitIds.interstitial}")
                }

                override fun onAdClicked() {
                    Log.d(Tag, "clicked format=interstitial placement=$placement unit=${AdUnitIds.interstitial}")
                }
            }

            runCatching { ad.show(activity) }
                .onFailure { throwable ->
                    Log.e(
                        Tag,
                        "show-exception placement=$placement unit=${AdUnitIds.interstitial} exception=${throwable.message}",
                        throwable,
                    )
                    finishOnce()
                }
        }
    }

    private fun preloadOnMain() {
        val context = appContext
        if (context == null) {
            Log.w(Tag, "preload-skipped reason=context-null")
            return
        }
        if (interstitialAd != null) {
            Log.d(Tag, "preload-skipped reason=already-loaded")
            return
        }
        if (loading) {
            Log.d(Tag, "preload-skipped reason=already-loading")
            return
        }

        loading = true
        retryRunnable?.let(mainHandler::removeCallbacks)
        retryRunnable = null
        AdManager.loadWhenReady(
            format = Format,
            adUnitId = AdUnitIds.interstitial,
            isActive = { loading && interstitialAd == null },
        ) {
            InterstitialAd.load(
                AdRequest.Builder(AdUnitIds.interstitial).build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        mainHandler.post {
                            loading = false
                            retryAttempt = 0
                            interstitialAd = ad
                            AdManager.onAdLoaded(Format, AdUnitIds.interstitial, ad.getResponseInfo())
                            Log.d(Tag, "preload-finished status=success")
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mainHandler.post {
                            loading = false
                            interstitialAd = null
                            retryAttempt += 1
                            AdManager.onAdFailed(Format, AdUnitIds.interstitial, adError)
                            scheduleRetry()
                        }
                    }
                },
            )
        }
    }

    private fun scheduleRetry() {
        val context = appContext ?: return
        val delayMs = min(60_000L, 1_000L * (1 shl min(retryAttempt, 6)))
        retryRunnable?.let(mainHandler::removeCallbacks)
        retryRunnable = Runnable {
            retryRunnable = null
            preload(context)
        }
        Log.d(Tag, "preload-retry-scheduled attempt=$retryAttempt delayMs=$delayMs")
        mainHandler.postDelayed(retryRunnable!!, delayMs)
    }
}
private fun Activity.restoreDefaultSystemBarLayout() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    ViewCompat.requestApplyInsets(window.decorView)
    window.decorView.post { ViewCompat.requestApplyInsets(window.decorView) }
}





