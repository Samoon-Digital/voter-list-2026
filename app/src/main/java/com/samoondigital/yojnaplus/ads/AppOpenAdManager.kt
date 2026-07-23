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
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

object AppOpenAdManager {
    private const val Tag = "AdMobAppOpen"
    private const val Format = "app-open"
    private const val MaxAdAgeMs = 4 * 60 * 60_000L
    private const val MaxShowsPerSession = 2

    private val mainHandler = Handler(Looper.getMainLooper())

    private var appContext: Application? = null
    private var currentActivityRef: WeakReference<Activity>? = null
    private var appOpenAd: AppOpenAd? = null
    private var loadTimeMs = 0L
    private var loading = false
    private var showing = false
    private var registered = false
    private var foregroundCount = 0
    private var sawFirstForeground = false
    private var sawFirstResume = false
    private var isHomeVisible = false
    private var homeOpportunityActive = true
    private var shownCount = 0
    private var shownFromHome = false
    private var shownFromForeground = false
    private var suppressNextResumeShow = false
    private var retryAttempt = 0
    private var retryRunnable: Runnable? = null

    fun register(application: Application) {
        if (registered) return
        registered = true
        appContext = application
        application.registerActivityLifecycleCallbacks(callbacks)
        Log.d(
            Tag,
            "registered maxAdAgeMs=$MaxAdAgeMs maxShowsPerSession=$MaxShowsPerSession",
        )
        preload(application)
    }

    fun preload(application: Application) {
        appContext = application
        mainHandler.post { preloadOnMain() }
    }

    fun setHomeScreenVisible(visible: Boolean) {
        mainHandler.post {
            val wasVisible = isHomeVisible
            isHomeVisible = visible

            if (wasVisible != visible) {
                Log.d(
                    Tag,
                    "home-visibility visible=$visible loaded=${appOpenAd != null} homeOpportunityActive=$homeOpportunityActive shownCount=$shownCount",
                )
            }

            if (!visible && homeOpportunityActive && !shownFromHome && shownCount == 0) {
                homeOpportunityActive = false
                Log.d(Tag, "home-show-window-closed action=cache-for-foreground")
            }

            if (visible) {
                showIfAvailable(ShowSource.Home)
            }
        }
    }

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity) {
            val wasBackground = foregroundCount == 0
            foregroundCount += 1
            currentActivityRef = WeakReference(activity)

            if (!wasBackground) return
            if (!sawFirstForeground) {
                sawFirstForeground = true
                Log.d(Tag, "foreground-skipped reason=cold-start")
                preload(activity.application)
                return
            }

            showIfAvailable(ShowSource.Foreground)
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivityRef = WeakReference(activity)
            if (!sawFirstResume) {
                sawFirstResume = true
                Log.d(Tag, "resume-skipped reason=first-resume")
                return
            }
            if (suppressNextResumeShow) {
                suppressNextResumeShow = false
                Log.d(Tag, "resume-skipped reason=post-ad-resume")
                return
            }

            showIfAvailable(ShowSource.Foreground)
        }

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) {
            foregroundCount = max(0, foregroundCount - 1)
            if (foregroundCount == 0) {
                Log.d(Tag, "backgrounded")
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivityRef?.get() === activity) currentActivityRef = null
        }
    }

    private fun showIfAvailable(source: ShowSource) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showIfAvailable(source) }
            return
        }
        if (showing) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=already-showing")
            return
        }
        if (shownCount >= MaxShowsPerSession) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=session-cap count=$shownCount")
            return
        }
        if (shownFromForeground) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=foreground-already-shown")
            return
        }
        if (source == ShowSource.Home && (!isHomeVisible || !homeOpportunityActive || shownFromHome)) {
            Log.d(
                Tag,
                "show-skipped source=home reason=not-eligible visible=$isHomeVisible homeOpportunityActive=$homeOpportunityActive shownFromHome=$shownFromHome",
            )
            return
        }

        val activity = currentActivityRef?.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=activity-unavailable")
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=not-loaded")
            maybePreloadForFuture(source, activity.application)
            return
        }

        if (!isAdFresh()) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=stale ageMs=${SystemClock.elapsedRealtime() - loadTimeMs}")
            appOpenAd = null
            maybePreloadForFuture(source, activity.application)
            return
        }

        appOpenAd = null
        showing = true
        if (source == ShowSource.Home) {
            homeOpportunityActive = false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                suppressNextResumeShow = true
                if (source == ShowSource.Home) {
                    shownFromHome = true
                } else {
                    shownFromForeground = true
                }
                Log.d(Tag, "show-started source=${source.logValue} unit=${AdUnitIds.appOpen}")
            }

            override fun onAdDismissedFullScreenContent() {
                showing = false
                shownCount += 1
                Log.d(
                    Tag,
                    "show-dismissed source=${source.logValue} count=$shownCount unit=${AdUnitIds.appOpen}",
                )
                maybePreloadAfterShow(source, activity.application)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false
                Log.w(
                    Tag,
                    "show-failed source=${source.logValue} unit=${AdUnitIds.appOpen} code=${error.code} domain=${error.domain} message=${error.message}",
                )
                maybePreloadAfterShow(source, activity.application)
            }

            override fun onAdImpression() {
                Log.d(Tag, "impression format=app-open source=${source.logValue} unit=${AdUnitIds.appOpen}")
            }

            override fun onAdClicked() {
                Log.d(Tag, "clicked format=app-open source=${source.logValue} unit=${AdUnitIds.appOpen}")
            }
        }

        runCatching { ad.show(activity) }
            .onFailure { throwable ->
                showing = false
                Log.e(
                    Tag,
                    "show-exception source=${source.logValue} unit=${AdUnitIds.appOpen} exception=${throwable.message}",
                    throwable,
                )
                maybePreloadAfterShow(source, activity.application)
            }
    }

    private fun preloadOnMain() {
        val context = appContext
        if (context == null) {
            Log.w(Tag, "preload-skipped reason=context-null")
            return
        }
        if (!canLoadMore()) {
            Log.d(
                Tag,
                "preload-skipped reason=session-state shownCount=$shownCount shownFromForeground=$shownFromForeground",
            )
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
            isActive = { loading && appOpenAd == null && canLoadMore() },
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
                        Log.d(
                            Tag,
                            "preload-finished status=success homeVisible=$isHomeVisible homeOpportunityActive=$homeOpportunityActive shownCount=$shownCount",
                        )
                        if (isHomeVisible) showIfAvailable(ShowSource.Home)
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

    private fun maybePreloadForFuture(source: ShowSource, application: Application) {
        if (source == ShowSource.Home || !shownFromForeground) preload(application)
    }

    private fun maybePreloadAfterShow(source: ShowSource, application: Application) {
        if (source == ShowSource.Home && canLoadMore()) {
            preload(application)
        } else {
            retryRunnable?.let(mainHandler::removeCallbacks)
            retryRunnable = null
            Log.d(
                Tag,
                "preload-skipped reason=post-show-policy source=${source.logValue} shownCount=$shownCount shownFromForeground=$shownFromForeground",
            )
        }
    }

    private fun scheduleRetry() {
        val context = appContext ?: return
        if (!canLoadMore()) return
        val delayMs = min(60_000L, 1_000L * (1 shl min(retryAttempt, 6)))
        retryRunnable?.let(mainHandler::removeCallbacks)
        retryRunnable = Runnable {
            retryRunnable = null
            preload(context)
        }
        Log.d(Tag, "preload-retry-scheduled attempt=$retryAttempt delayMs=$delayMs")
        mainHandler.postDelayed(retryRunnable!!, delayMs)
    }

    private fun canLoadMore(): Boolean =
        shownCount < MaxShowsPerSession && !shownFromForeground

    private fun isAdFresh(): Boolean =
        appOpenAd != null && SystemClock.elapsedRealtime() - loadTimeMs < MaxAdAgeMs

    private enum class ShowSource(val logValue: String) {
        Home("home"),
        Foreground("foreground"),
    }
}
