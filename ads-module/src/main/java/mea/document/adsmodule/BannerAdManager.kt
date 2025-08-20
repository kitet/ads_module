package mea.document.adsmodule

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

internal class BannerAdManager(
    private val context: Context,
    private val adUnitIdProvider: () -> String?
) {
    /**
     * Load a banner into the provided container. If isInline = true, use inline adaptive; else anchored adaptive.
     */
    fun loadInto(container: ViewGroup, isInline: Boolean) {
        val adUnitId = adUnitIdProvider() ?: return
        val adView = AdView(context)
        adView.adUnitId = adUnitId
        val adSize = determineAdaptiveSize(container, isInline)
        adView.setAdSize(adSize)

        container.removeAllViews()
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.addView(adView, layoutParams)

        adView.loadAd(AdRequest.Builder().build())
    }

    private fun determineAdaptiveSize(container: ViewGroup, isInline: Boolean): AdSize {
        val display = (container.context as? Activity)?.windowManager?.defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val density = outMetrics.density
        val containerWidthPx = container.width.takeIf { it > 0 } ?: outMetrics.widthPixels
        val adWidth = (containerWidthPx / density).toInt()

        return if (isInline) {
            AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(container.context, adWidth)
        } else {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(container.context, adWidth)
        }
    }
}


