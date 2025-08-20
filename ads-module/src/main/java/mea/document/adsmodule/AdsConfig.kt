package mea.document.adsmodule

import android.app.Application

data class AdsConfig(
    val application: Application,
    val appOpenAdUnitId: String? = null,
    val interstitialAdUnitId: String? = null,
    val bannerAdUnitId: String? = null,
    val enableAppOpenAds: Boolean = true,
    val delayFirstAppOpenMs: Long = 0L,
    val tagForChildDirectedTreatment: Boolean? = null,
    val tagForUnderAgeOfConsent: Boolean? = null,
    val maxAdContentRating: String? = null
)

object TestAdUnits {
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
}


