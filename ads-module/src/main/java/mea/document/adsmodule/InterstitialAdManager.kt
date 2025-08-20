package mea.document.adsmodule

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

internal class InterstitialAdManager(
    private val context: Context,
    private val adUnitIdProvider: () -> String?
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false
    private val isShowing: AtomicBoolean = AtomicBoolean(false)

    fun preload() {
        if (isLoading || interstitialAd != null) return
        val adUnitId = adUnitIdProvider() ?: return
        isLoading = true
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun show(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean {
        val ad = interstitialAd ?: run {
            preload()
            onFailed?.invoke()
            return false
        }
        if (isShowing.get()) return false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                isShowing.set(false)
                preload()
                onDismissed?.invoke()
            }
            override fun onAdShowedFullScreenContent() {
                isShowing.set(true)
            }
            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                isShowing.set(false)
                preload()
                onFailed?.invoke()
            }
        }
        ad.show(activity)
        return true
    }
}


