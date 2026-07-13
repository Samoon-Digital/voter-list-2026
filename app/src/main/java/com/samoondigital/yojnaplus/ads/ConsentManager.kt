package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {
    private const val Tag = "AdMobConsent"
    private var consentInformation: ConsentInformation? = null

    fun gatherConsent(activity: Activity, onAdsAllowed: () -> Unit) {
        val information = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = information
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

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let {
                Log.w(Tag, "privacy-options error code=${it.errorCode} message=${it.message}")
            }
        }
    }
}