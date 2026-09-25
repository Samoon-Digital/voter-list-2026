package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

object AppOpenAdManager {
    private const val Tag = "AdMobAppOpen"
    private const val StartupFormat = "app-open-startup"
    private const val ForegroundFormat = "app-open-foreground"
    private const val MaxAdAgeMs = 4 * 60 * 60_000L
    private const val MaxShowsPerSession = 2
    private const val ExternalFullScreenAdCooldownMs = 4_000L

    private val mainHandler = Handler(Looper.getMainLooper())

    private var appContext: Application? = null
    private var currentActivityRef: WeakReference<Activity>? = null
    private var startupAppOpenAd: AppOpenAd? = null
    private var startupLoadTimeMs = 0L
    private var startupLoading = false
    private var foregroundAppOpenAd: AppOpenAd? = null
    private var foregroundLoadTimeMs = 0L
    private var foregroundLoading = false
    private var showing = false
    private var registered = false
    private var foregroundCount = 0
    private var sawFirstForeground = false
    private var sawFirstResume = false
    private var coldStartSplashFinished = false
    private var isHomeVisible = false
    private var homeOpportunityActive = true
    private var shownCount = 0
    private var shownFromHome = false
    private var shownFromForeground = false
    private var suppressNextResumeShow = false
    private var externalFullScreenAdShowing = false
    private var externalFullScreenAdSuppressUntilMs = 0L
    private var startupRetryAttempt = 0
    private var startupRetryRunnable: Runnable? = null

    fun register(application: Application) {
        if (registered) return
        registered = true
        appContext = application
        application.registerActivityLifecycleCallbacks(callbacks)
        Log.d(
            Tag,
            "registered maxAdAgeMs=$MaxAdAgeMs maxShowsPerSession=$MaxShowsPerSession",
        )
        preloadStartup(application)
    }


    fun onExternalFullScreenAdWillShow() {
        mainHandler.post {
            externalFullScreenAdShowing = true
            suppressNextResumeShow = true
            externalFullScreenAdSuppressUntilMs = SystemClock.elapsedRealtime() + ExternalFullScreenAdCooldownMs
            Log.d(Tag, "external-fullscreen-started action=suppress-app-open")
        }
    }

    fun onExternalFullScreenAdFinished() {
        mainHandler.post {
            externalFullScreenAdShowing = false
            suppressNextResumeShow = true
            externalFullScreenAdSuppressUntilMs = SystemClock.elapsedRealtime() + ExternalFullScreenAdCooldownMs
            Log.d(Tag, "external-fullscreen-finished action=suppress-app-open")
        }
    }
    fun preload(application: Application) {
        preloadStartup(application)
    }

    private fun preloadStartup(application: Application) {
        appContext = application
        mainHandler.post { preloadStartupOnMain() }
    }

    fun onColdStartSplashFinished() {
        mainHandler.post {
            if (coldStartSplashFinished) return@post
            coldStartSplashFinished = true
            Log.d(Tag, "cold-start-splash-finished loaded=${startupAppOpenAd != null} homeVisible=$isHomeVisible")
            if (isHomeVisible) {
                showIfAvailable(ShowSource.Home)
            }
        }
    }

    fun setHomeScreenVisible(visible: Boolean) {
        mainHandler.post {
            val wasVisible = isHomeVisible
            isHomeVisible = visible

            if (wasVisible != visible) {
                Log.d(
                    Tag,
                    "home-visibility visible=$visible loaded=${startupAppOpenAd != null} homeOpportunityActive=$homeOpportunityActive shownCount=$shownCount",
                )
            }

            if (!visible && homeOpportunityActive && !shownFromHome && shownCount == 0) {
                homeOpportunityActive = false
                startupAppOpenAd = null
                Log.d(Tag, "home-show-window-closed action=discard-startup-cache")
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
                Log.d(Tag, "backgrounded action=preload-foreground-app-open")
                if (!showing && !externalFullScreenAdShowing) {
                    preloadForeground(activity.application)
                }
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
        val nowMs = SystemClock.elapsedRealtime()
        if (externalFullScreenAdShowing || nowMs < externalFullScreenAdSuppressUntilMs) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=external-fullscreen")
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
        if (source == ShowSource.Home && !coldStartSplashFinished) {
            Log.d(Tag, "show-skipped source=home reason=cold-start-splash")
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

        val ad = adFor(source)
        if (ad == null) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=not-loaded")
            maybePreloadForFuture(source, activity.application)
            return
        }

        if (!isAdFresh(source)) {
            Log.d(Tag, "show-skipped source=${source.logValue} reason=stale ageMs=${adAgeMs(source)}")
            clearAd(source)
            maybePreloadForFuture(source, activity.application)
            return
        }

        clearAd(source)
        showing = true
        if (source == ShowSource.Home) {
            homeOpportunityActive = false
        }

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                mainHandler.post {
                    suppressNextResumeShow = true
                    if (source == ShowSource.Home) {
                        shownFromHome = true
                    } else {
                        shownFromForeground = true
                    }
                    Log.d(Tag, "show-started source=${source.logValue} unit=${source.adUnitId}")
                }
            }

            override fun onAdDismissedFullScreenContent() {
                mainHandler.post {
                    showing = false
                    shownCount += 1
                    Log.d(
                        Tag,
                        "show-dismissed source=${source.logValue} count=$shownCount unit=${source.adUnitId}",
                    )
                    activity.restoreDefaultSystemBarLayout()
                    maybePreloadAfterShow(source, activity.application)
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                mainHandler.post {
                    showing = false
                    Log.w(
                        Tag,
                        "show-failed source=${source.logValue} unit=${source.adUnitId} code=${fullScreenContentError.code} message=${fullScreenContentError.message}",
                    )
                    activity.restoreDefaultSystemBarLayout()
                    maybePreloadAfterShow(source, activity.application)
                }
            }

            override fun onAdImpression() {
                Log.d(Tag, "impression format=app-open source=${source.logValue} unit=${source.adUnitId}")
            }

            override fun onAdClicked() {
                Log.d(Tag, "clicked format=app-open source=${source.logValue} unit=${source.adUnitId}")
            }
        }

        runCatching { ad.show(activity) }
            .onFailure { throwable ->
                showing = false
                Log.e(
                    Tag,
                    "show-exception source=${source.logValue} unit=${source.adUnitId} exception=${throwable.message}",
                    throwable,
                )
                activity.restoreDefaultSystemBarLayout()
                maybePreloadAfterShow(source, activity.application)
            }
    }

    private fun preloadStartupOnMain() {
        val context = appContext
        if (context == null) {
            Log.w(Tag, "preload-skipped source=home reason=context-null")
            return
        }
        if (!canLoadStartup()) {
            Log.d(
                Tag,
                "preload-skipped source=home reason=session-state shownCount=$shownCount shownFromHome=$shownFromHome homeOpportunityActive=$homeOpportunityActive",
            )
            return
        }
        if (startupLoading) {
            Log.d(Tag, "preload-skipped source=home reason=already-loading")
            return
        }
        if (startupAppOpenAd != null && isAdFresh(ShowSource.Home)) {
            Log.d(Tag, "preload-skipped source=home reason=already-loaded")
            return
        }
        if (startupAppOpenAd != null) {
            Log.d(Tag, "preload-discarded source=home reason=stale ageMs=${adAgeMs(ShowSource.Home)}")
            startupAppOpenAd = null
        }

        startupLoading = true
        startupRetryRunnable?.let(mainHandler::removeCallbacks)
        startupRetryRunnable = null
        AdManager.loadWhenReady(
            format = StartupFormat,
            adUnitId = AdUnitIds.appOpen,
            isActive = { startupLoading && startupAppOpenAd == null && canLoadStartup() },
        ) {
            AppOpenAd.load(
                AdRequest.Builder(AdUnitIds.appOpen).build(),
                object : AdLoadCallback<AppOpenAd> {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        mainHandler.post {
                            startupLoading = false
                            startupRetryAttempt = 0
                            startupAppOpenAd = ad
                            startupLoadTimeMs = SystemClock.elapsedRealtime()
                            AdManager.onAdLoaded(StartupFormat, AdUnitIds.appOpen, ad.getResponseInfo())
                            Log.d(
                                Tag,
                                "preload-finished status=success source=home homeVisible=$isHomeVisible homeOpportunityActive=$homeOpportunityActive shownCount=$shownCount",
                            )
                            if (isHomeVisible) showIfAvailable(ShowSource.Home)
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mainHandler.post {
                            startupLoading = false
                            startupAppOpenAd = null
                            startupRetryAttempt += 1
                            AdManager.onAdFailed(StartupFormat, AdUnitIds.appOpen, adError)
                            scheduleStartupRetry()
                        }
                    }
                },
            )
        }
    }

    private fun preloadForeground(application: Application) {
        appContext = application
        mainHandler.post { preloadForegroundOnMain() }
    }

    private fun preloadForegroundOnMain() {
        val context = appContext
        if (context == null) {
            Log.w(Tag, "preload-skipped source=foreground reason=context-null")
            return
        }
        if (!canLoadForeground()) {
            Log.d(
                Tag,
                "preload-skipped source=foreground reason=session-state shownCount=$shownCount shownFromForeground=$shownFromForeground",
            )
            return
        }
        if (foregroundLoading) {
            Log.d(Tag, "preload-skipped source=foreground reason=already-loading")
            return
        }
        if (foregroundAppOpenAd != null && isAdFresh(ShowSource.Foreground)) {
            Log.d(Tag, "preload-skipped source=foreground reason=already-loaded")
            return
        }
        if (foregroundAppOpenAd != null) {
            Log.d(Tag, "preload-discarded source=foreground reason=stale ageMs=${adAgeMs(ShowSource.Foreground)}")
            foregroundAppOpenAd = null
        }

        foregroundLoading = true
        AdManager.loadWhenReady(
            format = ForegroundFormat,
            adUnitId = AdUnitIds.foregroundAppOpen,
            isActive = { foregroundLoading && foregroundAppOpenAd == null && canLoadForeground() },
        ) {
            AppOpenAd.load(
                AdRequest.Builder(AdUnitIds.foregroundAppOpen).build(),
                object : AdLoadCallback<AppOpenAd> {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        mainHandler.post {
                            foregroundLoading = false
                            foregroundAppOpenAd = ad
                            foregroundLoadTimeMs = SystemClock.elapsedRealtime()
                            AdManager.onAdLoaded(ForegroundFormat, AdUnitIds.foregroundAppOpen, ad.getResponseInfo())
                            Log.d(Tag, "preload-finished status=success source=foreground")
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mainHandler.post {
                            foregroundLoading = false
                            foregroundAppOpenAd = null
                            AdManager.onAdFailed(ForegroundFormat, AdUnitIds.foregroundAppOpen, adError)
                            Log.d(Tag, "preload-retry-skipped source=foreground reason=wait-for-next-background")
                        }
                    }
                },
            )
        }
    }

    private fun maybePreloadForFuture(source: ShowSource, application: Application) {
        if (source == ShowSource.Home) {
            preloadStartup(application)
        }
    }

    private fun maybePreloadAfterShow(source: ShowSource, application: Application) {
        startupRetryRunnable?.let(mainHandler::removeCallbacks)
        startupRetryRunnable = null
        Log.d(
            Tag,
            "preload-skipped reason=post-show-policy source=${source.logValue} shownCount=$shownCount shownFromForeground=$shownFromForeground",
        )
    }

    private fun scheduleStartupRetry() {
        val context = appContext ?: return
        if (!canLoadStartup()) return
        val delayMs = min(60_000L, 1_000L * (1 shl min(startupRetryAttempt, 6)))
        startupRetryRunnable?.let(mainHandler::removeCallbacks)
        startupRetryRunnable = Runnable {
            startupRetryRunnable = null
            preloadStartup(context)
        }
        Log.d(Tag, "preload-retry-scheduled source=home attempt=$startupRetryAttempt delayMs=$delayMs")
        mainHandler.postDelayed(startupRetryRunnable!!, delayMs)
    }

    private fun canLoadStartup(): Boolean =
        shownCount < MaxShowsPerSession && !shownFromHome && homeOpportunityActive

    private fun canLoadForeground(): Boolean =
        shownCount < MaxShowsPerSession && !shownFromForeground

    private fun adFor(source: ShowSource): AppOpenAd? = when (source) {
        ShowSource.Home -> startupAppOpenAd
        ShowSource.Foreground -> foregroundAppOpenAd
    }

    private fun clearAd(source: ShowSource) {
        when (source) {
            ShowSource.Home -> startupAppOpenAd = null
            ShowSource.Foreground -> foregroundAppOpenAd = null
        }
    }

    private fun isAdFresh(source: ShowSource): Boolean =
        adFor(source) != null && adAgeMs(source) < MaxAdAgeMs

    private fun adAgeMs(source: ShowSource): Long {
        val loadTime = when (source) {
            ShowSource.Home -> startupLoadTimeMs
            ShowSource.Foreground -> foregroundLoadTimeMs
        }
        return SystemClock.elapsedRealtime() - loadTime
    }

    private enum class ShowSource(val logValue: String, val adUnitId: String) {
        Home("home", AdUnitIds.appOpen),
        Foreground("foreground", AdUnitIds.foregroundAppOpen),
    }
}
private fun Activity.restoreDefaultSystemBarLayout() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    ViewCompat.requestApplyInsets(window.decorView)
    window.decorView.post { ViewCompat.requestApplyInsets(window.decorView) }
}

