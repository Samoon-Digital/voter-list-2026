package com.samoondigital.yojnaplus

import android.app.Application
import com.samoondigital.yojnaplus.ads.AdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YojnaPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
