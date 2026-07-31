package com.samoondigital.yojnaplus.ads

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.initialization.InitializationStatus
import com.samoondigital.yojnaplus.BuildConfig

object AdUnitIds {
    val appOpen: String = BuildConfig.ADMOB_APP_OPEN_AD_UNIT_ID
    val banner: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    val interstitial: String = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
    val native: String = BuildConfig.ADMOB_NATIVE_AD_UNIT_ID

    fun expectedFor(format: String): String? = when (format) {
        "app-open" -> appOpen
        "banner" -> banner
        "interstitial" -> interstitial
        "native" -> native
        else -> null
    }
}

private object ProductionAdMobConfig {
    const val PackageName = "com.samoondigital.yojnaplus"
    const val AppId = "ca-app-pub-1638673809508848~3940017763"
    const val AppOpen = "ca-app-pub-1638673809508848/5780292909"
    const val Banner = "ca-app-pub-1638673809508848/5540207067"
    const val Interstitial = "ca-app-pub-1638673809508848/8518136887"
    const val Native = "ca-app-pub-1638673809508848/3565193102"
}

private object DebugAdMobConfig {
    const val AppId = "ca-app-pub-3940256099942544~3347511713"
    const val AppOpen = "ca-app-pub-3940256099942544/9257395921"
    const val Banner = "ca-app-pub-3940256099942544/6300978111"
    const val Interstitial = "ca-app-pub-3940256099942544/1033173712"
    const val Native = "ca-app-pub-3940256099942544/2247696110"
}

object AdManager {
    private const val Tag = "AdMob"
    private const val ManifestAppIdKey = "com.google.android.gms.ads.APPLICATION_ID"
    private const val AdsTemporarilyDisabled = false
    private enum class InitializationState { NotStarted, Initializing, Initialized }

    private class PendingLoad(val execute: () -> Unit)

    private data class ValidationResult(
        val valid: Boolean,
        val manifestAppId: String?,
        val reasons: List<String>,
    )

    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingLoads = mutableSetOf<PendingLoad>()
    private var state = InitializationState.NotStarted
    private var adRequestsAllowed = false
    private var application: Application? = null

    fun areAdsTemporarilyDisabled(): Boolean = AdsTemporarilyDisabled

    fun initialize(application: Application) {
        if (AdsTemporarilyDisabled) {
            this.application = application
            Log.d(Tag, "ads-temporarily-disabled initialize-skipped")
            return
        }
        val shouldInitialize = synchronized(lock) {
            if (state != InitializationState.NotStarted) {
                false
            } else {
                this.application = application
                state = InitializationState.Initializing
                true
            }
        }
        if (!shouldInitialize) {
            Log.d(Tag, "initialize skipped reason=already-started state=$state")
            return
        }

        logValidation("startup", null, validateRuntime(application, null, null, requireInitialized = false))
        Log.d(
            Tag,
            "initialize-start sdk=${MobileAds.getVersion()} package=${application.packageName} applicationId=${BuildConfig.APPLICATION_ID} buildType=${BuildConfig.BUILD_TYPE} debug=${BuildConfig.DEBUG}",
        )
        MobileAds.initialize(application) { initializationStatus ->
            synchronized(lock) { state = InitializationState.Initialized }
            Log.d(Tag, "initialize-finished sdk=${MobileAds.getVersion()} state=initialized")
            logInitialization(initializationStatus)
            drainPendingLoadsIfReady()
        }
    }

    fun allowAdRequests() {
        if (AdsTemporarilyDisabled) {
            Log.d(Tag, "ads-temporarily-disabled consent-gate-ignored")
            return
        }
        synchronized(lock) { adRequestsAllowed = true }
        Log.d(Tag, "consent-gate-open canRequestAds=true")
        drainPendingLoadsIfReady()
    }

    fun loadWhenReady(
        format: String,
        adUnitId: String,
        isActive: () -> Boolean,
        load: (AdRequest) -> Unit,
    ): () -> Unit {
        if (AdsTemporarilyDisabled) {
            Log.d(Tag, "ads-temporarily-disabled request-skipped format=$format unit=$adUnitId")
            return {}
        }
        val pending = PendingLoad {
            val app = application
            if (!isActive()) {
                Log.d(Tag, "request-skipped format=$format unit=$adUnitId reason=inactive")
                return@PendingLoad
            }
            if (app == null) {
                Log.e(Tag, "request-skipped format=$format unit=$adUnitId reason=application-null")
                return@PendingLoad
            }
            val validation = validateRuntime(app, format, adUnitId, requireInitialized = true)
            if (!validation.valid) {
                logValidation("request", format, validation)
                return@PendingLoad
            }
            logValidation("request", format, validation)
            logRequestStarted(format, adUnitId, app)
            runCatching { load(AdRequest.Builder().build()) }
                .onFailure { throwable ->
                    Log.e(
                        Tag,
                        "request-finished status=exception format=$format unit=$adUnitId exception=${throwable.message}",
                        throwable,
                    )
                }
        }
        val runNow = synchronized(lock) {
            if (state == InitializationState.Initialized && adRequestsAllowed) {
                true
            } else {
                pendingLoads += pending
                Log.d(
                    Tag,
                    "request-queued format=$format unit=$adUnitId initialized=${state == InitializationState.Initialized} consentReady=$adRequestsAllowed",
                )
                false
            }
        }
        if (runNow) postToMain(pending.execute)
        return { synchronized(lock) { pendingLoads.remove(pending) } }
    }

    fun onAdLoaded(format: String, adUnitId: String, responseInfo: ResponseInfo?) {
        Log.d(Tag, "request-finished status=success format=$format unit=$adUnitId")
        logResponse(format, adUnitId, responseInfo)
    }

    fun onAdFailed(format: String, adUnitId: String, error: LoadAdError) {
        val diagnosis = when {
            error.message.contains("Publisher data not found", ignoreCase = true) ->
                "publisher-data-missing: verify the unit, AdMob app/package pairing, account setup, and Policy Center"
            error.code == 3 -> "no-fill: inspect ResponseInfo, Ad Inspector, serving limits, and policy/account state"
            error.code == 2 -> "network-error: verify device connectivity, DNS/VPN/firewall, and Google Play services"
            else -> "load-failed"
        }
        Log.w(
            Tag,
            "request-finished status=failure diagnosis=$diagnosis format=$format unit=$adUnitId code=${error.code} domain=${error.domain} message=${error.message} cause=${error.cause} error=$error",
            RuntimeException("Ad load failure stack trace"),
        )
        logResponse(format, adUnitId, error.responseInfo)
    }

    fun openAdInspector(activity: Activity) {
        if (AdsTemporarilyDisabled) {
            Log.d(Tag, "ads-temporarily-disabled inspector-skipped")
            return
        }
        if (!BuildConfig.DEBUG) {
            Log.d(Tag, "ad-inspector-skipped reason=debug-only buildType=${BuildConfig.BUILD_TYPE}")
            return
        }
        MobileAds.openAdInspector(activity) { error ->
            if (error == null) {
                Log.d(Tag, "ad-inspector-closed")
            } else {
                Log.w(Tag, "ad-inspector-error code=${error.code} domain=${error.domain} message=${error.message}")
            }
        }
    }

    private fun drainPendingLoadsIfReady() {
        val loads = synchronized(lock) {
            if (state == InitializationState.Initialized && adRequestsAllowed) {
                pendingLoads.toList().also { pendingLoads.clear() }
            } else {
                emptyList()
            }
        }
        if (loads.isNotEmpty()) postToMain { loads.forEach { it.execute() } }
    }

    private fun validateRuntime(
        context: Context,
        format: String?,
        adUnitId: String?,
        requireInitialized: Boolean,
    ): ValidationResult {
        val manifestAppId = manifestAppId(context)
        val expectedAppId = if (BuildConfig.DEBUG) DebugAdMobConfig.AppId else ProductionAdMobConfig.AppId
        val expectedAppOpen = if (BuildConfig.DEBUG) DebugAdMobConfig.AppOpen else ProductionAdMobConfig.AppOpen
        val expectedBanner = if (BuildConfig.DEBUG) DebugAdMobConfig.Banner else ProductionAdMobConfig.Banner
        val expectedInterstitial = if (BuildConfig.DEBUG) DebugAdMobConfig.Interstitial else ProductionAdMobConfig.Interstitial
        val expectedNative = if (BuildConfig.DEBUG) DebugAdMobConfig.Native else ProductionAdMobConfig.Native
        val configuredIds = linkedMapOf(
            "appId" to BuildConfig.ADMOB_APP_ID,
            "appOpen" to AdUnitIds.appOpen,
            "banner" to AdUnitIds.banner,
            "interstitial" to AdUnitIds.interstitial,
            "native" to AdUnitIds.native,
        )
        val reasons = mutableListOf<String>()

        if (context.packageName != ProductionAdMobConfig.PackageName) {
            reasons += "runtime package ${context.packageName} != ${ProductionAdMobConfig.PackageName}"
        }
        if (BuildConfig.APPLICATION_ID != ProductionAdMobConfig.PackageName) {
            reasons += "BuildConfig.APPLICATION_ID ${BuildConfig.APPLICATION_ID} != ${ProductionAdMobConfig.PackageName}"
        }
        if (manifestAppId != expectedAppId) {
            reasons += "manifest App ID $manifestAppId != $expectedAppId"
        }
        if (BuildConfig.ADMOB_APP_ID != expectedAppId) {
            reasons += "BuildConfig ADMOB_APP_ID ${BuildConfig.ADMOB_APP_ID} != $expectedAppId"
        }
        if (AdUnitIds.appOpen != expectedAppOpen) {
            reasons += "App Open ID ${AdUnitIds.appOpen} != $expectedAppOpen"
        }
        if (AdUnitIds.banner != expectedBanner) {
            reasons += "Banner ID ${AdUnitIds.banner} != $expectedBanner"
        }
        if (AdUnitIds.interstitial != expectedInterstitial) {
            reasons += "Interstitial ID ${AdUnitIds.interstitial} != $expectedInterstitial"
        }
        if (AdUnitIds.native != expectedNative) {
            reasons += "Native ID ${AdUnitIds.native} != $expectedNative"
        }
        configuredIds.forEach { (name, value) ->
            if (value.isBlank()) reasons += "$name is blank"
        }
        if (format != null && adUnitId != null) {
            val expectedForFormat = AdUnitIds.expectedFor(format)
            if (expectedForFormat == null) {
                reasons += "unknown ad format $format"
            } else if (expectedForFormat != adUnitId) {
                reasons += "requested $format unit $adUnitId != expected $expectedForFormat"
            }
        }
        if (!hasPermission(context, Manifest.permission.INTERNET)) {
            reasons += "missing android.permission.INTERNET"
        }
        if (!hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            reasons += "missing android.permission.ACCESS_NETWORK_STATE"
        }
        if (requireInitialized && !isInitialized()) {
            reasons += "Google Mobile Ads SDK is not initialized"
        }

        return ValidationResult(reasons.isEmpty(), manifestAppId, reasons)
    }

    private fun logValidation(stage: String, format: String?, validation: ValidationResult) {
        val status = if (validation.valid) "passed" else "failed"
        val context = application
        val details = buildString {
            append("validation-$status stage=$stage")
            format?.let { append(" format=$it") }
            append(" package=${context?.packageName}")
            append(" applicationId=${BuildConfig.APPLICATION_ID}")
            append(" manifestAppId=${validation.manifestAppId}")
            append(" buildType=${BuildConfig.BUILD_TYPE}")
            append(" initialized=${isInitialized()}")
            append(" appId=${BuildConfig.ADMOB_APP_ID}")
            append(" appOpen=${AdUnitIds.appOpen}")
            append(" banner=${AdUnitIds.banner}")
            append(" interstitial=${AdUnitIds.interstitial}")
            append(" native=${AdUnitIds.native}")
            if (!validation.valid) append(" reasons=${validation.reasons.joinToString("; ")}")
        }
        if (validation.valid) Log.d(Tag, details) else Log.e(Tag, details)
    }

    private fun logRequestStarted(format: String, adUnitId: String, context: Context) {
        Log.d(
            Tag,
            "request-started format=$format unit=$adUnitId appId=${BuildConfig.ADMOB_APP_ID} package=${context.packageName} applicationId=${BuildConfig.APPLICATION_ID} buildType=${BuildConfig.BUILD_TYPE} debug=${BuildConfig.DEBUG} initialized=${isInitialized()} consentReady=$adRequestsAllowed network=${networkDescription(context)} sdk=${MobileAds.getVersion()}",
        )
    }

    private fun logInitialization(initializationStatus: InitializationStatus) {
        initializationStatus.adapterStatusMap.forEach { (adapter, status) ->
            Log.d(
                Tag,
                "adapter adapter=$adapter state=${status.initializationState} latencyMs=${status.latency} description=${status.description}",
            )
        }
    }

    private fun logResponse(format: String, adUnitId: String, responseInfo: ResponseInfo?) {
        val loadedAdapter = responseInfo?.loadedAdapterResponseInfo
        Log.d(
            Tag,
            "response format=$format unit=$adUnitId responseId=${responseInfo?.responseId} mediationAdapter=${responseInfo?.mediationAdapterClassName} loadedAdapter=${loadedAdapter?.adapterClassName} loadedSource=${loadedAdapter?.adSourceName} latencyMs=${loadedAdapter?.latencyMillis} adapters=${responseInfo?.adapterResponses}",
        )
    }

    private fun manifestAppId(context: Context): String? = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
            ?.getString(ManifestAppIdKey)
    }.getOrNull()

    private fun hasPermission(context: Context, permission: String): Boolean =
        context.packageManager.checkPermission(permission, context.packageName) == PackageManager.PERMISSION_GRANTED

    private fun isInitialized(): Boolean = synchronized(lock) { state == InitializationState.Initialized }

    private fun postToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private fun networkDescription(context: Context): String {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return "unavailable"
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return "offline"
        return buildString {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> append("wifi")
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> append("cellular")
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> append("ethernet")
                else -> append("other")
            }
            append(if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) ":validated" else ":unvalidated")
        }
    }
}
