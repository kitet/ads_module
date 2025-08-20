package mea.document.adsmodule.demo

import android.app.Application
import mea.document.adsmodule.AdsConfig
import mea.document.adsmodule.AdsManager
import mea.document.adsmodule.TestAdUnits

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdsManager.initialize(
            AdsConfig(
                application = this,
                appOpenAdUnitId = TestAdUnits.APP_OPEN,
                interstitialAdUnitId = TestAdUnits.INTERSTITIAL,
                bannerAdUnitId = TestAdUnits.BANNER,
                enableAppOpenAds = true,
                delayFirstAppOpenMs = 3000L
            )
        )
    }
}


