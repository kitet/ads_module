package mea.document.adsmodule

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

internal class ConsentManager(
    private val context: Context,
    private val config: AdsConfig
) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    fun requestConsent(activity: Activity, onFinished: (Boolean) -> Unit) {
        val paramsBuilder = ConsentRequestParameters.Builder()
        config.tagForUnderAgeOfConsent?.let { underAge ->
            paramsBuilder.setTagForUnderAgeOfConsent(underAge)
        }

        // Optional debug settings could be added here for test devices
        // val debugSettings = ConsentDebugSettings.Builder(context).build()
        // paramsBuilder.setConsentDebugSettings(debugSettings)

        val params = paramsBuilder.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated.
                if (consentInformation.isConsentFormAvailable) {
                    loadAndShowForm(activity, onFinished)
                } else {
                    onFinished(isConsentObtained())
                }
            },
            {
                onFinished(isConsentObtained())
            }
        )
    }

    private fun loadAndShowForm(activity: Activity, onFinished: (Boolean) -> Unit) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
            activity
        ) { formError ->
            // If an error occurs, proceed without blocking ads loading
            onFinished(isConsentObtained())
        }
    }

    fun isConsentObtained(): Boolean {
        return consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED
    }
}


