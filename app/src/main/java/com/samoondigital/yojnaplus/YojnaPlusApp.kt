package com.samoondigital.yojnaplus

import android.app.Application
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.notifications.FirebaseIntegrationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YojnaPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
        AppOpenAdManager.register(this)
        InterstitialAdManager.preload(this)
        FirebaseIntegrationManager.initialize(this)
    }
}
