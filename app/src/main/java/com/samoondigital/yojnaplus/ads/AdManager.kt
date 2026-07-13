package com.samoondigital.yojnaplus.ads

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
import com.samoondigital.yojnaplus.BuildConfig

object AdUnitIds {
    val banner: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    val native: String = BuildConfig.ADMOB_NATIVE_AD_UNIT_ID
}

object AdManager {
    private const val Tag = "AdMob"
    private const val GoogleDemoPublisher = "ca-app-pub-3940256099942544"
    private const val ManifestAppIdKey = "com.google.android.gms.ads.APPLICATION_ID"

    private enum class InitializationState { NotStarted, Initializing, Initialized }

    private class PendingLoad(val execute: () -> Unit)

    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingLoads = mutableSetOf<PendingLoad>()
    private var state = InitializationState.NotStarted
    private var application: Application? = null

    fun initialize(application: Application) {
        val shouldInitialize = synchronized(lock) {
            if (state != InitializationState.NotStarted) {
                false
            } else {
                this.application = application
                state = InitializationState.Initializing
                true
            }
        }
        if (!shouldInitialize) return

        validateConfiguration(application)
        Log.d(Tag, "initialize sdk=${MobileAds.getVersion()} package=${application.packageName} applicationId=${BuildConfig.APPLICATION_ID} debug=${BuildConfig.DEBUG} testAds=${BuildConfig.ADMOB_USES_TEST_ADS}")
        MobileAds.initialize(application) { initializationStatus ->
            val loads = synchronized(lock) {
                state = InitializationState.Initialized
                pendingLoads.toList().also { pendingLoads.clear() }
            }
            logInitialization(initializationStatus)
            postToMain { loads.forEach { it.execute() } }
        }
    }

    fun loadWhenReady(
        format: String,
        adUnitId: String,
        isActive: () -> Boolean,
        load: (AdRequest) -> Unit,
    ): () -> Unit {
        val pending = PendingLoad {
            if (isActive()) {
                logRequest(format, adUnitId)
                load(AdRequest.Builder().build())
            }
        }
        val runNow = synchronized(lock) {
            if (state == InitializationState.Initialized) {
                true
            } else {
                pendingLoads += pending
                false
            }
        }
        if (runNow) postToMain(pending.execute)
        return { synchronized(lock) { pendingLoads.remove(pending) } }
    }

    fun onAdLoaded(format: String, adUnitId: String, responseInfo: ResponseInfo?) {
        Log.d(Tag, "loaded format=$format unit=$adUnitId")
        logResponse(format, adUnitId, responseInfo)
    }

    fun onAdFailed(format: String, adUnitId: String, error: LoadAdError) {
        val diagnosis = when {
            error.message.contains("Publisher data not found", ignoreCase = true) ->
                "publisher-data-missing: verify the unit, AdMob app/package pairing, account setup, and Policy Center"
            error.code == 3 -> "no-fill: this can be inventory-related; inspect ResponseInfo and Ad Inspector"
            else -> "load-failed"
        }
        Log.w(
            Tag,
            "$diagnosis format=$format unit=$adUnitId code=${error.code} domain=${error.domain} message=${error.message} cause=${error.cause}",
        )
        logResponse(format, adUnitId, error.responseInfo)
    }

    fun openAdInspector(activity: Activity) {
        if (!BuildConfig.DEBUG) return
        MobileAds.openAdInspector(activity) { error ->
            if (error == null) {
                Log.d(Tag, "Ad Inspector closed")
            } else {
                Log.w(Tag, "Ad Inspector error code=${error.code} domain=${error.domain} message=${error.message}")
            }
        }
    }

    private fun validateConfiguration(context: Context) {
        val manifestAppId = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                .metaData
                ?.getString(ManifestAppIdKey)
        }.getOrNull()
        val demoValues = listOf(BuildConfig.ADMOB_APP_ID, AdUnitIds.banner, AdUnitIds.native)
            .all { it.startsWith(GoogleDemoPublisher) }

        if (manifestAppId != BuildConfig.ADMOB_APP_ID) {
            Log.e(Tag, "configuration-error: manifestAppId=$manifestAppId does not match BuildConfig appId=${BuildConfig.ADMOB_APP_ID}")
        }
        if (context.packageName != BuildConfig.APPLICATION_ID) {
            Log.e(Tag, "configuration-error: runtime package=${context.packageName} does not match BuildConfig applicationId=${BuildConfig.APPLICATION_ID}")
        }
        if (BuildConfig.ADMOB_USES_TEST_ADS != demoValues) {
            Log.e(Tag, "configuration-error: test/release AdMob values are mixed for buildType=${BuildConfig.BUILD_TYPE}")
        }
        if (AdUnitIds.banner.isBlank() || AdUnitIds.native.isBlank()) {
            Log.e(Tag, "configuration-error: banner and native ad unit IDs must both be configured")
        }
    }

    private fun logRequest(format: String, adUnitId: String) {
        val context = application
        Log.d(
            Tag,
            "request format=$format unit=$adUnitId appId=${BuildConfig.ADMOB_APP_ID} package=${context?.packageName} applicationId=${BuildConfig.APPLICATION_ID} buildType=${BuildConfig.BUILD_TYPE} debug=${BuildConfig.DEBUG} testAds=${BuildConfig.ADMOB_USES_TEST_ADS} initialized=${isInitialized()} network=${context?.let(::networkDescription)} sdk=${MobileAds.getVersion()}",
        )
    }

    private fun logInitialization(initializationStatus: com.google.android.gms.ads.initialization.InitializationStatus) {
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