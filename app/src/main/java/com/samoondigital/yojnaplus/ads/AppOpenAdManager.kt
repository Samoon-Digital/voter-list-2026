package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.lang.ref.WeakReference

/** One bounded startup opportunity per process. Home and later resumes cannot open a new one. */
object AppOpenAdManager {
    private const val Tag = "AdMobAppOpen"
    private const val Format = "app-open-startup"
    private const val MaxAdAgeMs = 4 * 60 * 60_000L
    private const val ExternalFullScreenAdCooldownMs = 4_000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gate = StartupAdGate()
    internal val startupPhase = gate.phase
    private var startupAd: AppOpenAd? = null
    private var loadedAtMs = 0L
    private var cancelPendingLoad: (() -> Unit)? = null
    private var externalAdShowing = false
    private var externalAdSuppressUntilMs = 0L
    private var timeout: Runnable? = null
    private var hostActivity: WeakReference<Activity>? = null
    private var appReady = false
    private var consentReady = false

    /** Start SDK initialization in parallel with the first frame; consent still gates ad requests. */
    internal fun preload(application: Application) {
        if (AdManager.areAdsTemporarilyDisabled()) {
            skipStartup("ads-disabled")
            return
        }
        AdManager.initialize(application)
        if (!gate.startLoading()) return
        Log.d(Tag, "startup-request-queued")
        cancelPendingLoad = AdManager.loadWhenReady(
            Format, AdUnitIds.appOpen, { gate.acceptsLoadedAd(SystemClock.elapsedRealtime()) },
        ) {
            try {
                AppOpenAd.load(
                    AdRequest.Builder(AdUnitIds.appOpen).build(),
                    object : AdLoadCallback<AppOpenAd> {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            mainHandler.post {
                                if (!gate.acceptsLoadedAd(SystemClock.elapsedRealtime())) {
                                    ad.destroy()
                                    if (gate.expire(SystemClock.elapsedRealtime())) releasePendingAd()
                                    Log.d(Tag, "startup-load-discarded reason=window-closed")
                                    return@post
                                }
                                startupAd = ad
                                loadedAtMs = SystemClock.elapsedRealtime()
                                AdManager.onAdLoaded(Format, AdUnitIds.appOpen, ad.getResponseInfo())
                                tryShow()
                            }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            mainHandler.post {
                                AdManager.onAdFailed(Format, AdUnitIds.appOpen, adError)
                                skipStartup("load-failed")
                            }
                        }
                    },
                )
            } catch (error: Exception) {
                Log.e(Tag, "startup-load-exception", error)
                skipStartup("load-exception")
            }
        }
    }

    /** Count the six seconds from visible loading, without resetting the clock on recreation. */
    internal fun onFirstFrame() {
        if (!gate.startWindow(SystemClock.elapsedRealtime())) return
        Log.d(Tag, "startup-window-opened maxWaitMs=${gate.remainingWaitMs(SystemClock.elapsedRealtime())}")
        timeout = Runnable {
            if (gate.expire(SystemClock.elapsedRealtime())) {
                releasePendingAd()
                Log.d(Tag, "startup-finished action=skip reason=deadline")
            }
        }.also { mainHandler.postDelayed(it, gate.remainingWaitMs(SystemClock.elapsedRealtime())) }
    }

    /** Readiness signals can arrive in any order; loading continues until the six-second deadline. */
    internal fun onAppReady(activity: Activity, consentFlowComplete: Boolean) {
        if (gate.phase.value != StartupAdPhase.Preparing) return
        hostActivity = WeakReference(activity)
        appReady = true
        consentReady = consentFlowComplete
        tryShow()
    }

    internal fun onHostResumed(activity: Activity) {
        if (hostActivity?.get() === activity) tryShow()
    }

    private fun tryShow() {
        if (gate.phase.value != StartupAdPhase.Preparing) return
        val activity = hostActivity?.get()
        val ad = startupAd
        val fresh = ad != null && SystemClock.elapsedRealtime() - loadedAtMs < MaxAdAgeMs
        val resumed = (activity as? LifecycleOwner)?.lifecycle?.currentState
            ?.isAtLeast(Lifecycle.State.RESUMED) == true
        val eligible = appReady && consentReady && resumed &&
            !activity.isFinishing && !activity.isDestroyed &&
            !externalAdShowing && SystemClock.elapsedRealtime() >= externalAdSuppressUntilMs
        val shouldShow = gate.tryShow(fresh, eligible, SystemClock.elapsedRealtime())
        if (gate.phase.value == StartupAdPhase.Finished) {
            releasePendingAd()
            Log.d(Tag, "startup-finished action=skip reason=deadline")
            return
        }
        if (!shouldShow || ad == null || activity == null) return
        cancelPendingLoad?.invoke()
        cancelPendingLoad = null
        timeout?.let(mainHandler::removeCallbacks)
        timeout = null
        startupAd = null

        fun finish() {
            if (gate.phase.value != StartupAdPhase.Showing) return
            ad.destroy()
            if (!activity.isDestroyed) {
                WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                ViewCompat.requestApplyInsets(activity.window.decorView)
            }
            gate.finish()
            hostActivity = null
        }
        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                Log.d(Tag, "show-started source=startup unit=${AdUnitIds.appOpen}")
            }

            override fun onAdDismissedFullScreenContent() {
                mainHandler.post { finish() }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                mainHandler.post {
                    Log.w(Tag, "startup-show-failed error=$fullScreenContentError")
                    finish()
                }
            }

            override fun onAdImpression() {
                Log.d(Tag, "impression format=app-open source=startup unit=${AdUnitIds.appOpen}")
            }

            override fun onAdClicked() {
                Log.d(Tag, "clicked format=app-open source=startup unit=${AdUnitIds.appOpen}")
            }
        }
        runCatching { ad.show(activity) }.onFailure {
            Log.e(Tag, "startup-show-exception", it)
            finish()
        }
    }

    internal fun skipStartup(reason: String) {
        if (gate.phase.value != StartupAdPhase.Preparing) return
        gate.finish()
        releasePendingAd()
        Log.d(Tag, "startup-finished action=skip reason=$reason")
    }

    private fun releasePendingAd() {
        timeout?.let(mainHandler::removeCallbacks)
        timeout = null
        cancelPendingLoad?.invoke()
        cancelPendingLoad = null
        startupAd?.destroy()
        startupAd = null
        hostActivity = null
    }

    fun onExternalFullScreenAdWillShow() {
        mainHandler.post {
            externalAdShowing = true
            externalAdSuppressUntilMs = SystemClock.elapsedRealtime() + ExternalFullScreenAdCooldownMs
        }
    }

    fun onExternalFullScreenAdFinished() {
        mainHandler.post {
            externalAdShowing = false
            externalAdSuppressUntilMs = SystemClock.elapsedRealtime() + ExternalFullScreenAdCooldownMs
        }
    }
}
