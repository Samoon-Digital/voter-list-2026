package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {
    private const val Tag = "AdMobConsent"

    fun gatherConsent(activity: Activity, onAdsAllowed: () -> Unit) {
        if (AdManager.areAdsTemporarilyDisabled()) {
            Log.d(Tag, "ads-temporarily-disabled consent-skipped")
            return
        }
        val information = UserMessagingPlatform.getConsentInformation(activity)
        if (information.canRequestAds()) onAdsAllowed()

        information.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let {
                        Log.w(Tag, "consent-form error code=${it.errorCode} message=${it.message}")
                    }
                    if (information.canRequestAds()) onAdsAllowed()
                }
            },
            { requestError ->
                Log.w(Tag, "consent-info error code=${requestError.errorCode} message=${requestError.message}")
                if (information.canRequestAds()) onAdsAllowed()
            },
        )
    }
}
