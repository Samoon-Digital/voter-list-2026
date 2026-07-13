package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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

    fun preload(context: Context) {
        appContext = context.applicationContext
        mainHandler.post { preloadOnMain() }
    }

    fun showIfAvailable(activity: Activity?, onContinue: () -> Unit) {
        if (activity == null) {
            Log.d(Tag, "show-skipped reason=activity-null")
            appContext?.let(::preload)
            onContinue()
            return
        }

        mainHandler.post {
            val ad = interstitialAd
            if (ad == null) {
                Log.d(Tag, "show-skipped reason=not-loaded")
                preload(activity.applicationContext)
                onContinue()
                return@post
            }

            interstitialAd = null
            val continued = AtomicBoolean(false)
            fun continueOnce() {
                if (continued.compareAndSet(false, true)) onContinue()
            }
            fun clearAndPreload() {
                preload(activity.applicationContext)
            }

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(Tag, "show-started unit=${AdUnitIds.interstitial}")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(Tag, "show-dismissed unit=${AdUnitIds.interstitial}")
                    clearAndPreload()
                    continueOnce()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(
                        Tag,
                        "show-failed unit=${AdUnitIds.interstitial} code=${error.code} domain=${error.domain} message=${error.message}",
                    )
                    clearAndPreload()
                    continueOnce()
                }

                override fun onAdImpression() {
                    Log.d(Tag, "impression format=interstitial unit=${AdUnitIds.interstitial}")
                }

                override fun onAdClicked() {
                    Log.d(Tag, "clicked format=interstitial unit=${AdUnitIds.interstitial}")
                }
            }

            runCatching { ad.show(activity) }
                .onFailure { throwable ->
                    Log.e(Tag, "show-exception unit=${AdUnitIds.interstitial} exception=${throwable.message}", throwable)
                    clearAndPreload()
                    continueOnce()
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
        ) { request ->
            InterstitialAd.load(
                context,
                AdUnitIds.interstitial,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        loading = false
                        retryAttempt = 0
                        interstitialAd = ad
                        AdManager.onAdLoaded(Format, AdUnitIds.interstitial, ad.responseInfo)
                        Log.d(Tag, "preload-finished status=success")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loading = false
                        interstitialAd = null
                        retryAttempt += 1
                        AdManager.onAdFailed(Format, AdUnitIds.interstitial, error)
                        scheduleRetry()
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