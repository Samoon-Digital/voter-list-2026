package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AdUnitIds {
    const val Native = "ca-app-pub-1638673809508848/4367138885"
    const val Banner = "ca-app-pub-1638673809508848/3437200595"
    const val Interstitial = ""
    const val AppOpen = ""
}

object AdManager {
    private const val Tag = "AdManager"
    private const val NativePoolSize = 4
    private const val AppOpenCooldownMs = 60_000L
    private val main = Handler(Looper.getMainLooper())
    private val lock = Any()
    private val nativePool = ArrayDeque<NativeAd>()
    private val _nativeSignal = MutableStateFlow(0L)
    val nativeSignal: StateFlow<Long> = _nativeSignal

    private lateinit var app: Application
    private var initialized = false
    private var nativeLoading = 0
    private var nativeFailures = 0
    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var interstitialFailures = 0
    private var appOpen: AppOpenAd? = null
    private var appOpenLoading = false
    private var appOpenShowing = false
    private var appOpenFailures = 0
    private var lastFullScreenAt = 0L

    fun initialize(application: Application) {
        if (initialized) return
        initialized = true
        app = application
        MobileAds.initialize(application) {
            log("MobileAds initialized")
            warmNativePool()
            preloadInterstitial()
            preloadAppOpen()
        }
    }

    fun takeNativeAd(): NativeAd? = synchronized(lock) { nativePool.removeFirstOrNull() }
        .also { warmNativePool() }

    fun warmNativePool() {
        if (!initialized || AdUnitIds.Native.isBlank()) return
        synchronized(lock) {
            while (nativePool.size + nativeLoading < NativePoolSize) {
                nativeLoading++
                loadNative()
            }
        }
    }

    fun preloadInterstitial() {
        if (!initialized || AdUnitIds.Interstitial.isBlank() || interstitial != null || interstitialLoading) return
        interstitialLoading = true
        InterstitialAd.load(app, AdUnitIds.Interstitial, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                interstitialLoading = false
                interstitialFailures = 0
                log("Interstitial loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialLoading = false
                interstitial = null
                retry(++interstitialFailures) { preloadInterstitial() }
                warn("Interstitial failed: ${error.message}")
            }
        })
    }

    fun showInterstitial(activity: Activity, then: () -> Unit) {
        val ad = interstitial ?: return then().also { preloadInterstitial() }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() { log("Interstitial impression") }
            override fun onAdClicked() { log("Interstitial clicked") }
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = finish().also { warn("Interstitial show failed: ${error.message}") }
            private fun finish() {
                lastFullScreenAt = SystemClock.elapsedRealtime()
                preloadInterstitial()
                then()
            }
        }
        log("Interstitial show")
        ad.show(activity)
    }

    fun preloadAppOpen() {
        if (!initialized || AdUnitIds.AppOpen.isBlank() || appOpen != null || appOpenLoading) return
        appOpenLoading = true
        AppOpenAd.load(app, AdUnitIds.AppOpen, AdRequest.Builder().build(), object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpen = ad
                appOpenLoading = false
                appOpenFailures = 0
                log("AppOpen loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                appOpenLoading = false
                appOpen = null
                retry(++appOpenFailures) { preloadAppOpen() }
                warn("AppOpen failed: ${error.message}")
            }
        })
    }

    fun showAppOpenIfReady(activity: Activity) {
        val now = SystemClock.elapsedRealtime()
        if (appOpenShowing || now - lastFullScreenAt < AppOpenCooldownMs) return
        val ad = appOpen ?: return preloadAppOpen()
        appOpen = null
        appOpenShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() { log("AppOpen impression") }
            override fun onAdClicked() { log("AppOpen clicked") }
            override fun onAdDismissedFullScreenContent() = done("dismissed")
            override fun onAdFailedToShowFullScreenContent(error: AdError) = done("show failed: ${error.message}")
            private fun done(msg: String) {
                log("AppOpen $msg")
                appOpenShowing = false
                lastFullScreenAt = SystemClock.elapsedRealtime()
                preloadAppOpen()
            }
        }
        log("AppOpen show")
        ad.show(activity)
    }

    fun destroy() {
        synchronized(lock) {
            nativePool.forEach { it.destroy() }
            nativePool.clear()
        }
        interstitial = null
        appOpen = null
    }

    private fun loadNative() {
        AdLoader.Builder(app, AdUnitIds.Native)
            .forNativeAd { ad ->
                synchronized(lock) {
                    nativeLoading--
                    nativeFailures = 0
                    nativePool.addLast(ad)
                }
                _nativeSignal.value++
                log("Native loaded")
                warmNativePool()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    synchronized(lock) { nativeLoading-- }
                    retry(++nativeFailures) { warmNativePool() }
                    warn("Native failed: ${error.message}")
                }

                override fun onAdImpression() { log("Native impression") }
                override fun onAdClicked() { log("Native clicked") }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun retry(failures: Int, block: () -> Unit) {
        main.postDelayed(block, listOf(5_000L, 15_000L, 30_000L, 60_000L).getOrElse(failures - 1) { 60_000L })
    }

    private fun log(message: String) = Log.d(Tag, message)
    private fun warn(message: String) = Log.w(Tag, message)
}