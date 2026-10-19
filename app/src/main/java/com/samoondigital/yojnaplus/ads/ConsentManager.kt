package com.samoondigital.yojnaplus.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConsentManager {
    private const val Tag = "AdMobConsent"
    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired = _privacyOptionsRequired.asStateFlow()
    private var privacyFormShowing = false

    private fun updatePrivacyOptions(information: ConsentInformation) {
        _privacyOptionsRequired.value = information.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptions(activity: Activity, onError: () -> Unit) {
        if (privacyFormShowing || activity.isFinishing || activity.isDestroyed) return
        val information = UserMessagingPlatform.getConsentInformation(activity)
        updatePrivacyOptions(information)
        if (!_privacyOptionsRequired.value) return
        privacyFormShowing = true
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            privacyFormShowing = false
            updatePrivacyOptions(information)
            if (information.canRequestAds()) AdManager.allowAdRequests()
            error?.let {
                Log.w(Tag, "privacy-options error code=${it.errorCode} message=${it.message}")
                onError()
            }
        }
    }

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
                updatePrivacyOptions(information)
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    updatePrivacyOptions(information)
                    formError?.let {
                        Log.w(Tag, "consent-form error code=${it.errorCode} message=${it.message}")
                    }
                    if (information.canRequestAds()) onAdsAllowed()
                }
            },
            { requestError ->
                updatePrivacyOptions(information)
                Log.w(Tag, "consent-info error code=${requestError.errorCode} message=${requestError.message}")
                if (information.canRequestAds()) onAdsAllowed()
            },
        )
        updatePrivacyOptions(information)
    }
}
