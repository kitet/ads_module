package mea.document.adsmodule

import android.app.Activity
import android.view.ViewGroup
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

object AdsManager {
    @Volatile
    private var initialized: Boolean = false

    private lateinit var config: AdsConfig
    private lateinit var consentManager: ConsentManager
    private var appOpenAdManager: AppOpenAdManager? = null
    private var interstitialAdManager: InterstitialAdManager? = null
    private var bannerAdManager: BannerAdManager? = null

    fun initialize(config: AdsConfig, onReady: (() -> Unit)? = null) {
        synchronized(this) {
            if (initialized) {
                onReady?.invoke()
                return
            }
            this.config = config
            // Apply request configuration (TFCD/TFUA/max content rating)
            val requestConfigBuilder = RequestConfiguration.Builder()
            config.tagForChildDirectedTreatment?.let {
                requestConfigBuilder.setTagForChildDirectedTreatment(
                    if (it) RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE else RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                )
            }
            config.tagForUnderAgeOfConsent?.let {
                requestConfigBuilder.setTagForUnderAgeOfConsent(
                    if (it) RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE else RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
                )
            }
            config.maxAdContentRating?.let { rating ->
                val mapped = when (rating.uppercase()) {
                    "G" -> RequestConfiguration.MAX_AD_CONTENT_RATING_G
                    "PG" -> RequestConfiguration.MAX_AD_CONTENT_RATING_PG
                    "T" -> RequestConfiguration.MAX_AD_CONTENT_RATING_T
                    "MA" -> RequestConfiguration.MAX_AD_CONTENT_RATING_MA
                    else -> null
                }
                mapped?.let { requestConfigBuilder.setMaxAdContentRating(it) }
            }
            MobileAds.setRequestConfiguration(requestConfigBuilder.build())
            MobileAds.initialize(config.application) {}
            consentManager = ConsentManager(config.application, config)
            if (config.enableAppOpenAds) {
                appOpenAdManager = AppOpenAdManager(
                    config.application,
                    { config.appOpenAdUnitId },
                    config.delayFirstAppOpenMs
                ).also { it.loadAd() }
            }
            interstitialAdManager = InterstitialAdManager(config.application, { config.interstitialAdUnitId }).also { it.preload() }
            bannerAdManager = BannerAdManager(config.application, { config.bannerAdUnitId })
            initialized = true
            onReady?.invoke()
        }
    }

    fun requestConsent(activity: Activity, onFinished: (Boolean) -> Unit) {
        if (!initialized) return onFinished(false)
        consentManager.requestConsent(activity, onFinished)
    }

    fun showAppOpenIfAvailable(activity: Activity) {
        appOpenAdManager?.showAdIfAvailable(activity)
    }

    fun preloadInterstitial() {
        interstitialAdManager?.preload()
    }

    fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean {
        return interstitialAdManager?.show(activity, onDismissed, onFailed) ?: false
    }

    // Alias aligning with requested API name
    fun showInterstitialAds(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean {
        return showInterstitial(activity, onDismissed, onFailed)
    }

    fun loadBanner(container: ViewGroup, isInline: Boolean) {
        bannerAdManager?.loadInto(container, isInline)
    }

    // Feature Module Simplified APIs
    // These methods are designed for feature modules that want simple, direct access

    /**
     * Simple interstitial show for feature modules.
     * Follows Google's best practices without retry mechanisms to avoid policy violations.
     */
    fun showInterstitialSimple(activity: Activity): Boolean {
        if (!initialized) return false
        return interstitialAdManager?.show(activity, null, null) ?: false
    }

    /**
     * Simple banner load for feature modules.
     * No callbacks needed, just load and forget.
     */
    fun loadBannerSimple(container: ViewGroup, isInline: Boolean = true) {
        if (!initialized) return
        bannerAdManager?.loadInto(container, isInline)
    }

    /**
     * Check if ads are ready to use (initialized by host app)
     */
    fun isReady(): Boolean = initialized

    /**
     * Get current initialization status for debugging
     */
    fun getStatus(): String {
        return if (initialized) {
            "Initialized - Ads ready to use"
        } else {
            "Not initialized - Host app must call AdsManager.initialize() first"
        }
    }
}


