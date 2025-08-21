package mea.document.adsmodule

import android.app.Activity
import android.view.ViewGroup

/**
 * Clean API facade for ads management.
 * 
 * This class provides a simple, vendor-agnostic interface for ads functionality.
 * All underlying SDK details are hidden internally, making it easy to swap
 * ad vendors (AdMob, Meta, etc.) without breaking other modules.
 */
object AdsManager {
    @Volatile
    private var initialized: Boolean = false

    private lateinit var config: AdsConfig
    private lateinit var consentManager: ConsentManager
    private var appOpenAdManager: AppOpenAdManager? = null
    private var interstitialAdManager: InterstitialAdManager? = null
    private var bannerAdManager: BannerAdManager? = null

    // ============================================================================
    // INITIALIZATION
    // ============================================================================

    /**
     * Initialize the ads module with configuration.
     * Call this once in your Application.onCreate()
     */
    fun initialize(config: AdsConfig, onReady: (() -> Unit)? = null) {
        synchronized(this) {
            if (initialized) {
                onReady?.invoke()
                return
            }
            this.config = config
            initializeSdk(config)
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

    // ============================================================================
    // CONSENT MANAGEMENT
    // ============================================================================

    /**
     * Request user consent for personalized ads.
     * Call this early in your app lifecycle.
     */
    fun requestConsent(activity: Activity, onFinished: (Boolean) -> Unit) {
        if (!initialized) return onFinished(false)
        consentManager.requestConsent(activity, onFinished)
    }

    // ============================================================================
    // APP OPEN ADS
    // ============================================================================

    /**
     * Show app open ad if available.
     * Call this when app returns to foreground.
     */
    fun showAppOpenIfAvailable(activity: Activity) {
        appOpenAdManager?.showAdIfAvailable(activity)
    }

    // ============================================================================
    // INTERSTITIAL ADS - CLEAN API
    // ============================================================================

    /**
     * Preload interstitial ad for better performance.
     */
    fun preloadInterstitial() {
        interstitialAdManager?.preload()
    }

    /**
     * Show interstitial ad with callbacks.
     * 
     * @param activity Current activity
     * @param onDismissed Called when ad is dismissed
     * @param onFailed Called when ad fails to show
     * @return true if ad was shown, false otherwise
     */
    fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean {
        return interstitialAdManager?.show(activity, onDismissed, onFailed) ?: false
    }

    /**
     * Show interstitial ad - simple one-liner for feature modules.
     * 
     * @param activity Current activity
     * @return true if ad was shown, false otherwise
     */
    fun showInterstitialAd(activity: Activity): Boolean {
        if (!initialized) return false
        return interstitialAdManager?.show(activity, null, null) ?: false
    }

    /**
     * Show interstitial ad at natural break points.
     * Perfect for level completion, purchase attempts, etc.
     * 
     * @param activity Current activity
     * @return true if ad was shown, false otherwise
     */
    fun showInterstitialAtBreakPoint(activity: Activity): Boolean {
        return showInterstitialAd(activity)
    }

    // ============================================================================
    // BANNER ADS - CLEAN API
    // ============================================================================

    /**
     * Load banner ad into container.
     * 
     * @param container ViewGroup to load banner into
     * @param isInline true for inline banner, false for anchored (bottom/top)
     */
    fun loadBanner(container: ViewGroup, isInline: Boolean) {
        bannerAdManager?.loadInto(container, isInline)
    }

    /**
     * Load inline banner ad.
     * Use for banners within content flow.
     * 
     * @param container ViewGroup to load banner into
     */
    fun loadInlineBanner(container: ViewGroup) {
        loadBanner(container, isInline = true)
    }

    /**
     * Load anchored banner ad (bottom of screen).
     * Use for banners fixed to screen edge.
     * 
     * @param container ViewGroup to load banner into
     */
    fun loadAnchoredBanner(container: ViewGroup) {
        loadBanner(container, isInline = false)
    }

    /**
     * Load banner ad with automatic positioning.
     * 
     * @param container ViewGroup to load banner into
     * @param position BannerPosition.INLINE or BannerPosition.ANCHORED
     */
    fun loadBannerAd(container: ViewGroup, position: BannerPosition = BannerPosition.INLINE) {
        loadBanner(container, position == BannerPosition.INLINE)
    }

    // ============================================================================
    // STATUS & UTILITIES
    // ============================================================================

    /**
     * Check if ads module is ready to use.
     */
    fun isReady(): Boolean = initialized

    /**
     * Get current initialization status for debugging.
     */
    fun getStatus(): String {
        return if (initialized) {
            "Ads module initialized and ready"
        } else {
            "Ads module not initialized"
        }
    }

    // ============================================================================
    // LEGACY API SUPPORT (for backward compatibility)
    // ============================================================================

    /**
     * @deprecated Use showInterstitialAd() instead
     */
    @Deprecated("Use showInterstitialAd() for cleaner API", ReplaceWith("showInterstitialAd(activity)"))
    fun showInterstitialAds(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean {
        return showInterstitial(activity, onDismissed, onFailed)
    }

    /**
     * @deprecated Use showInterstitialAd() instead
     */
    @Deprecated("Use showInterstitialAd() for cleaner API", ReplaceWith("showInterstitialAd(activity)"))
    fun showInterstitialSimple(activity: Activity): Boolean {
        return showInterstitialAd(activity)
    }

    /**
     * @deprecated Use loadBannerAd() instead
     */
    @Deprecated("Use loadBannerAd() for cleaner API", ReplaceWith("loadBannerAd(container, if (isInline) BannerPosition.INLINE else BannerPosition.ANCHORED)"))
    fun loadBannerSimple(container: ViewGroup, isInline: Boolean = true) {
        loadBanner(container, isInline)
    }

    // ============================================================================
    // PRIVATE IMPLEMENTATION DETAILS
    // ============================================================================

    private fun initializeSdk(config: AdsConfig) {
        // SDK initialization details are hidden here
        // This makes it easy to swap ad vendors in the future
        com.google.android.gms.ads.MobileAds.initialize(config.application) {}
        
        val requestConfigBuilder = com.google.android.gms.ads.RequestConfiguration.Builder()
        config.tagForChildDirectedTreatment?.let {
            requestConfigBuilder.setTagForChildDirectedTreatment(
                if (it) com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE 
                else com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
            )
        }
        config.tagForUnderAgeOfConsent?.let {
            requestConfigBuilder.setTagForUnderAgeOfConsent(
                if (it) com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE 
                else com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
            )
        }
        config.maxAdContentRating?.let { rating ->
            val mapped = when (rating.uppercase()) {
                "G" -> com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G
                "PG" -> com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_PG
                "T" -> com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T
                "MA" -> com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_MA
                else -> null
            }
            mapped?.let { requestConfigBuilder.setMaxAdContentRating(it) }
        }
        com.google.android.gms.ads.MobileAds.setRequestConfiguration(requestConfigBuilder.build())
    }
}

/**
 * Banner positioning options.
 */
enum class BannerPosition {
    /** Banner within content flow */
    INLINE,
    /** Banner anchored to screen edge (bottom/top) */
    ANCHORED
}


