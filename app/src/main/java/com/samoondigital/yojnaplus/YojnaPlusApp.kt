package com.samoondigital.yojnaplus

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YojnaPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess()) {
            Log.d(Tag, "secondary-process-init-skipped process=${currentProcessName().orEmpty()}")
            return
        }
        AdManager.initialize(this)
        AppOpenAdManager.register(this)
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
