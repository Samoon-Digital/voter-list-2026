package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlin.math.max
import kotlin.math.min

object AppOpenAdManager {
    private const val Tag = "AdMobAppOpen"
    private const val Format = "app-open"
    private const val BackgroundThresholdMs = 25_000L
    private const val CooldownMs = 7 * 60_000L
    private const val MaxAdAgeMs = 4 * 60 * 60_000L

    private val mainHandler = Handler(Looper.getMainLooper())

    private var appContext: Application? = null
    private var appOpenAd: AppOpenAd? = null
    private var loadTimeMs = 0L
    private var loading = false
    private var showing = false
    private var registered = false
    private var foregroundCount = 0
    private var sawFirstForeground = false
    private var backgroundedAtMs = 0L
    private var lastShownAtMs = 0L
    private var retryAttempt = 0
    private var retryRunnable: Runnable? = null
    private var currentActivity: Activity? = null

    fun register(application: Application) {
        if (registered) return
        registered = true
        appContext = application
        application.registerActivityLifecycleCallbacks(callbacks)
        Log.d(Tag, "registered backgroundThresholdMs=$BackgroundThresholdMs cooldownMs=$CooldownMs maxAdAgeMs=$MaxAdAgeMs")
        preload(application)
    }

    fun preload(application: Application) {
        appContext = application
        mainHandler.post { preloadOnMain() }
    }

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity) {
            val wasBackground = foregroundCount == 0
            foregroundCount += 1
            currentActivity = activity
            if (wasBackground) handleForeground(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) {
            foregroundCount = max(0, foregroundCount - 1)
            if (foregroundCount == 0) {
                backgroundedAtMs = SystemClock.elapsedRealtime()
                Log.d(Tag, "backgrounded atMs=$backgroundedAtMs")
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivity === activity) currentActivity = null
        }
    }

    private fun handleForeground(activity: Activity) {
        if (!sawFirstForeground) {
            sawFirstForeground = true
            Log.d(Tag, "show-skipped reason=cold-start")
            preload(activity.application)
            return
        }

        val now = SystemClock.elapsedRealtime()
        val backgroundDurationMs = if (backgroundedAtMs > 0L) now - backgroundedAtMs else 0L
        if (backgroundDurationMs < BackgroundThresholdMs) {
            Log.d(Tag, "show-skipped reason=background-too-short durationMs=$backgroundDurationMs")
            preload(activity.application)
            return
        }

        val cooldownRemainingMs = CooldownMs - (now - lastShownAtMs)
        if (lastShownAtMs > 0L && cooldownRemainingMs > 0L) {
            Log.d(Tag, "show-skipped reason=cooldown remainingMs=$cooldownRemainingMs")
            preload(activity.application)
            return
        }

        showIfAvailable(activity)
    }

    private fun showIfAvailable(activity: Activity) {
        if (showing) {
            Log.d(Tag, "show-skipped reason=already-showing")
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            Log.d(Tag, "show-skipped reason=not-loaded")
            preload(activity.application)
            return
        }

        if (!isAdFresh()) {
            Log.d(Tag, "show-skipped reason=stale ageMs=${SystemClock.elapsedRealtime() - loadTimeMs}")
            appOpenAd = null
            preload(activity.application)
            return
        }

        appOpenAd = null
        showing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(Tag, "show-started unit=${AdUnitIds.appOpen}")
            }

            override fun onAdDismissedFullScreenContent() {
                showing = false
                lastShownAtMs = SystemClock.elapsedRealtime()
                Log.d(Tag, "show-dismissed unit=${AdUnitIds.appOpen}")
                preload(activity.application)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false
                Log.w(Tag, "show-failed unit=${AdUnitIds.appOpen} code=${error.code} domain=${error.domain} message=${error.message}")
                preload(activity.application)
            }

            override fun onAdImpression() {
                Log.d(Tag, "impression format=app-open unit=${AdUnitIds.appOpen}")
            }

            override fun onAdClicked() {
                Log.d(Tag, "clicked format=app-open unit=${AdUnitIds.appOpen}")
            }
        }

        runCatching { ad.show(activity) }
            .onFailure { throwable ->
                showing = false
                Log.e(Tag, "show-exception unit=${AdUnitIds.appOpen} exception=${throwable.message}", throwable)
                preload(activity.application)
            }
    }

    private fun preloadOnMain() {
        val context = appContext
        if (context == null) {
            Log.w(Tag, "preload-skipped reason=context-null")
            return
        }
        if (loading) {
            Log.d(Tag, "preload-skipped reason=already-loading")
            return
        }
        if (appOpenAd != null && isAdFresh()) {
            Log.d(Tag, "preload-skipped reason=already-loaded")
            return
        }
        if (appOpenAd != null) {
            Log.d(Tag, "preload-discarded reason=stale ageMs=${SystemClock.elapsedRealtime() - loadTimeMs}")
            appOpenAd = null
        }

        loading = true
        retryRunnable?.let(mainHandler::removeCallbacks)
        retryRunnable = null
        AdManager.loadWhenReady(
            format = Format,
            adUnitId = AdUnitIds.appOpen,
            isActive = { loading && appOpenAd == null },
        ) { request ->
            AppOpenAd.load(
                context,
                AdUnitIds.appOpen,
                request,
                object : AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        loading = false
                        retryAttempt = 0
                        appOpenAd = ad
                        loadTimeMs = SystemClock.elapsedRealtime()
                        AdManager.onAdLoaded(Format, AdUnitIds.appOpen, ad.responseInfo)
                        Log.d(Tag, "preload-finished status=success")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loading = false
                        appOpenAd = null
                        retryAttempt += 1
                        AdManager.onAdFailed(Format, AdUnitIds.appOpen, error)
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

    private fun isAdFresh(): Boolean =
        appOpenAd != null && SystemClock.elapsedRealtime() - loadTimeMs < MaxAdAgeMs
}