package mea.document.adsmodule

import android.app.Activity
import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import java.util.concurrent.atomic.AtomicBoolean

internal class AppOpenAdManager(
    private val application: Application,
    private val adUnitIdProvider: () -> String?,
    private val delayFirstShowMs: Long
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd: Boolean = false
    private val isShowingAd: AtomicBoolean = AtomicBoolean(false)
    private var loadTimeElapsedRealtime: Long = 0L
    private var appStartElapsedRealtime: Long = SystemClock.elapsedRealtime()
    
    // Track application background state
    private var isAppInBackground: Boolean = false
    private var lastBackgroundTime: Long = 0L
    private val minBackgroundTimeMs: Long = 1000L // Minimum time in background to show App Open ad

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun isAdAvailable(): Boolean {
        val ad = appOpenAd ?: return false
        val elapsedMs = SystemClock.elapsedRealtime() - loadTimeElapsedRealtime
        // App open ads expire after 4 hours.
        return elapsedMs < 4 * 60 * 60 * 1000
    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return
        val adUnitId = adUnitIdProvider() ?: return
        isLoadingAd = true
        AppOpenAd.load(
            application,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTimeElapsedRealtime = SystemClock.elapsedRealtime()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd.get()) return
        
        // Check if app was actually in background for sufficient time
        val timeInBackground = SystemClock.elapsedRealtime() - lastBackgroundTime
        if (!isAppInBackground || timeInBackground < minBackgroundTimeMs) {
            return
        }
        
        val isColdStart = (SystemClock.elapsedRealtime() - appStartElapsedRealtime) < delayFirstShowMs
        if (isColdStart && delayFirstShowMs > 0) return

        if (!isAdAvailable()) {
            loadAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowingAd.set(true)
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd.set(false)
                loadAd()
            }
        }

        appOpenAd?.show(activity)
    }
    
    // Listen to application lifecycle, not activity lifecycle
    override fun onStart(owner: LifecycleOwner) {
        // App is coming to foreground
        if (isAppInBackground) {
            // App was in background, now returning to foreground
            currentActivity?.let { showAdIfAvailable(it) }
        }
        isAppInBackground = false
    }

    override fun onStop(owner: LifecycleOwner) {
        // App is going to background
        isAppInBackground = true
        lastBackgroundTime = SystemClock.elapsedRealtime()
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd.get()) currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}
}


