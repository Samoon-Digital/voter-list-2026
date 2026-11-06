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

/** One startup opportunity per process. Home visibility and activity resumes never show ads. */
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

    /** Called after the first loading frame, before theme preparation or deferred startup work. */
    internal fun preload(application: Application) {
        if (AdManager.areAdsTemporarilyDisabled()) {
            skipStartup("ads-disabled")
            return
        }
        AdManager.initialize(application)
        if (!gate.startLoading()) return
        Log.d(Tag, "startup-request-queued")
        cancelPendingLoad = AdManager.loadWhenReady(Format, AdUnitIds.appOpen, gate::acceptsLoadedAd) {
            try {
                AppOpenAd.load(
                    AdRequest.Builder(AdUnitIds.appOpen).build(),
                    object : AdLoadCallback<AppOpenAd> {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            mainHandler.post {
                                if (!gate.acceptsLoadedAd()) {
                                    ad.destroy()
                                    Log.d(Tag, "startup-load-discarded reason=content-ready")
                                    return@post
                                }
                                startupAd = ad
                                loadedAtMs = SystemClock.elapsedRealtime()
                                AdManager.onAdLoaded(Format, AdUnitIds.appOpen, ad.getResponseInfo())
                                // Loading an ad must never trigger a late show over app content.
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

    /** Decide once when real app preparation finishes; never wait for an ad to load. */
    internal fun onAppReady(activity: Activity, consentFlowComplete: Boolean) {
        if (gate.phase.value != StartupAdPhase.Preparing) return
        val ad = startupAd
        val fresh = ad != null && SystemClock.elapsedRealtime() - loadedAtMs < MaxAdAgeMs
        val resumed = (activity as? LifecycleOwner)?.lifecycle?.currentState
            ?.isAtLeast(Lifecycle.State.RESUMED) == true
        val eligible = consentFlowComplete && resumed && !activity.isFinishing && !activity.isDestroyed &&
            !externalAdShowing && SystemClock.elapsedRealtime() >= externalAdSuppressUntilMs
        val shouldShow = gate.onAppReady(adAvailable = fresh, eligible = eligible)
        cancelPendingLoad?.invoke()
        cancelPendingLoad = null
        startupAd = null
        if (!shouldShow || ad == null) {
            ad?.destroy()
            Log.d(Tag, "startup-finished action=skip adReady=$fresh eligible=$eligible")
            return
        }

        fun finish() {
            if (gate.phase.value != StartupAdPhase.Showing) return
            ad.destroy()
            if (!activity.isDestroyed) {
                WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                ViewCompat.requestApplyInsets(activity.window.decorView)
            }
            gate.finish()
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
        cancelPendingLoad?.invoke()
        cancelPendingLoad = null
        startupAd?.destroy()
        startupAd = null
        Log.d(Tag, "startup-finished action=skip reason=$reason")
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
