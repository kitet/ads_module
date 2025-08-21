package mea.document.adsmodule.demo

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import mea.document.adsmodule.AdsManager
import mea.document.adsmodule.demo.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_consent).setOnClickListener {
            AdsManager.requestConsent(this) { }
        }
        findViewById<Button>(R.id.btn_interstitial).setOnClickListener {
            AdsManager.showInterstitialAd(this)
        }
        

        
        // Load inline banner (within content)
        val inlineContainer = findViewById<FrameLayout>(R.id.inline_banner_container)
        inlineContainer.post {
            AdsManager.loadInlineBanner(inlineContainer)
        }
        
        // Load anchored banner (bottom of screen)
        val bottomContainer = findViewById<FrameLayout>(R.id.bottom_banner_container)
        bottomContainer.post {
            AdsManager.loadAnchoredBanner(bottomContainer)
        }
    }
}


