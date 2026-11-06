package com.samoondigital.yojnaplus

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.analytics.AppscopeTracker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YojnaPlusApp : Application() {
    private var deferredStartupInitialized = false

    /** Main-thread initialization after app content has had a frame to render. */
    fun initializeDeferredStartup() {
        if (deferredStartupInitialized) return
        if (!isMainProcess()) {
            Log.d(Tag, "secondary-process-init-skipped process=${currentProcessName().orEmpty()}")
            return
        }
        deferredStartupInitialized = true
        AppscopeTracker.initialize(this)
        InterstitialAdManager.preload(this)
    }

    private fun isMainProcess(): Boolean {
        val processName = currentProcessName() ?: return true
        return processName == packageName
    }

    private fun currentProcessName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getProcessName()
        }
        val currentPid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return activityManager
            ?.runningAppProcesses
            ?.firstOrNull { it.pid == currentPid }
            ?.processName
    }

    private companion object {
        const val Tag = "YojnaPlusApp"
    }
}
