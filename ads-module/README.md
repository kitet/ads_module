# Ads Module

A plug-and-play Android library for integrating Google AdMob ads with UMP consent management.

## Features

- **UMP Consent Management** - Handles user consent for personalized ads
- **App Open Ads** - Shows when app returns to foreground
- **Interstitial Ads** - Full-screen ads with preloading
- **Adaptive Banner Ads** - Both inline and anchored (bottom) variants
- **Single Source of Truth** - Centralized ads management

## Quick Start

### 1. Add Dependencies

#### For Host Apps (Final APK)
Add the AAR and include all required dependencies:

```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    
    // Required dependencies for host app
    implementation 'com.google.android.gms:play-services-ads:24.5.0'
    implementation 'com.google.android.ump:user-messaging-platform:3.2.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.3'
    implementation 'androidx.lifecycle:lifecycle-process:2.8.3'
}
```

#### For Feature Modules (compileOnly)
If you're building a feature module that will be included in a host app:

```gradle
dependencies {
    compileOnly fileTree(dir: 'libs', include: ['*.aar'])
    
    // compileOnly for module - host app will provide implementation
    compileOnly 'com.google.android.gms:play-services-ads:24.5.0'
    compileOnly 'com.google.android.ump:user-messaging-platform:3.2.0'
    compileOnly 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.3'
    compileOnly 'androidx.lifecycle:lifecycle-process:2.8.3'
}
```

**Important**: When using `compileOnly`, the host app must include the actual `implementation` dependencies.

### 2. Add AdMob App ID

Add to your `AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
</application>
```

### 3. Initialize

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AdsManager.initialize(
            AdsConfig(
                application = this,
                appOpenAdUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/1234567890",
                interstitialAdUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/0987654321",
                bannerAdUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/1122334455",
                enableAppOpenAds = true,
                delayFirstAppOpenMs = 3000L, // Avoid cold-start clashes
                tagForChildDirectedTreatment = null,
                tagForUnderAgeOfConsent = null,
                maxAdContentRating = "G" // G, PG, T, or MA
            )
        )
    }
}
```

## API Reference

### AdsManager

Main entry point for all ads functionality.

#### Initialization
```kotlin
fun initialize(config: AdsConfig, onReady: (() -> Unit)? = null)
```

#### Consent Management
```kotlin
fun requestConsent(activity: Activity, onFinished: (Boolean) -> Unit)
```

#### App Open Ads
```kotlin
fun showAppOpenIfAvailable(activity: Activity)
```

#### Interstitial Ads
```kotlin
fun preloadInterstitial()
fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean
fun showInterstitialAds(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean
```

#### Banner Ads
```kotlin
fun loadBanner(container: ViewGroup, isInline: Boolean)
```

#### Feature Module Simplified APIs
```kotlin
fun showInterstitialSimple(activity: Activity): Boolean
fun loadBannerSimple(container: ViewGroup, isInline: Boolean = true)
fun isReady(): Boolean
fun getStatus(): String
```

### AdsConfig

Configuration for the ads module.

```kotlin
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
```

### TestAdUnits

Pre-configured test ad unit IDs for development:

```kotlin
object TestAdUnits {
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
}
```

## Usage Examples

### Requesting Consent

```kotlin
AdsManager.requestConsent(activity) { consentGranted ->
    if (consentGranted) {
        // User granted consent, ads can be personalized
    } else {
        // User denied consent, show non-personalized ads
    }
}
```

### Showing Interstitial

```kotlin
// Preload for better performance
AdsManager.preloadInterstitial()

// Show when ready
val shown = AdsManager.showInterstitial(activity) {
    // Ad was dismissed
    println("Interstitial dismissed")
} {
    // Ad failed to show
    println("Interstitial failed")
}
```

### Loading Banner Ads

#### Inline Banner (within content)
```kotlin
val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
bannerContainer.post {
    AdsManager.loadBanner(bannerContainer, isInline = true)
}
```

#### Anchored Banner (bottom of screen)
```kotlin
val bottomBannerContainer = findViewById<FrameLayout>(R.id.bottom_banner)
bottomBannerContainer.post {
    AdsManager.loadBanner(bottomBannerContainer, isInline = false)
}
```

### Feature Module Usage (Simplified)

For feature modules that want simple, direct access without callbacks:

```kotlin
// Show interstitial - no callbacks needed!
AdsManager.showInterstitialSimple(activity)

// Show interstitial following Google's best practices
AdsManager.showInterstitialSimple(activity)

// Load banner - defaults to inline
AdsManager.loadBannerSimple(container)

// Load anchored banner at bottom
AdsManager.loadBannerSimple(container, isInline = false)

// Check if ads are ready
if (AdsManager.isReady()) {
    AdsManager.showInterstitialSimple(activity)
}

// Get status for debugging
println(AdsManager.getStatus())
```

### Complete Feature Module Example

```kotlin
class MyFeatureActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load banner at bottom
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        AdsManager.loadBannerSimple(bannerContainer, isInline = false)
    }
    
    fun onLevelComplete() {
        // Simple one-liner - no callbacks!
        AdsManager.showInterstitialSimple(this)
    }
    
    fun onPurchaseAttempt(): Boolean {
        // Returns true if ad was shown
        return AdsManager.showInterstitialSimple(this)
    }
    
    fun onCriticalMoment() {
        // Show interstitial for important moments
        AdsManager.showInterstitialSimple(this)
    }
    
    fun onPremiumFeature() {
        // Show interstitial for premium features
        val adShown = AdsManager.showInterstitialSimple(this)
        if (adShown) {
            unlockPremiumFeature()
        } else {
            // Still unlock even if ad fails
            unlockPremiumFeature()
        }
    }
}
```

### Compose Integration

```kotlin
@Composable
fun BannerAdView(modifier: Modifier = Modifier, isInline: Boolean = true) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context)
        },
        update = { frame ->
            frame.post {
                AdsManager.loadBanner(frame, isInline)
            }
        }
    )
}
```

## Best Practices

### Dependency Management
- **Host Apps**: Use `implementation` for all dependencies
- **Feature Modules**: Use `compileOnly` to avoid duplicate dependencies
- **Multi-module Projects**: Ensure only one module provides `implementation` dependencies
- **AAR Distribution**: Include AAR in each module's `libs` folder that needs ads functionality

### Feature Module Best Practices
- Use `showInterstitialSimple()` and `loadBannerSimple()` for clean, callback-free code
- Check `isReady()` before showing ads to avoid errors
- Don't worry about initialization - host app handles it
- Use `getStatus()` for debugging during development
- Follow Google's [interstitial ad best practices](https://developers.google.com/admob/android/interstitial) to avoid policy violations

### App Open Ads
- Set `delayFirstAppOpenMs` to avoid showing ads immediately on cold start
- App open ads are automatically shown when app returns to foreground
- Only show after user has used app a few times

### Banner Ads
- **Inline**: Use `isInline = true` for banners within content flow
- **Anchored**: Use `isInline = false` for banners at bottom/top of screen
- Container width determines adaptive banner size
- Always call `loadBanner` in `post { }` to ensure container is measured

### Interstitial Ads
- Preload interstitials for better user experience
- Show at natural break points (level completion, etc.)
- Don't show too frequently to avoid user frustration
- Follow Google's [interstitial ad guidelines](https://developers.google.com/admob/android/interstitial) for policy compliance

### Consent
- Request consent early in app lifecycle
- Respect user's choice and don't repeatedly ask
- Handle both granted and denied consent gracefully

## Troubleshooting

### Common Issues

1. **ClassNotFoundException for Google Play Services**
   - **Host App**: Ensure you've added the required dependencies with `implementation`
   - **Feature Module**: Use `compileOnly` and ensure host app provides `implementation`
   - AAR doesn't include transitive dependencies

2. **Module can't find AdsManager methods**
   - Use `compileOnly` dependency in feature modules
   - Host app must provide actual implementation dependencies
   - Check that AAR is properly included in module's libs folder

3. **Banner not showing**
   - Check container has proper width/height
   - Call `loadBanner` in `post { }` after layout
   - Verify ad unit ID is correct

4. **App Open ads not showing**
   - Check `enableAppOpenAds` is true in config
   - Verify app open ad unit ID is set
   - App must be backgrounded and returned to foreground

5. **Interstitial not loading**
   - Call `preloadInterstitial()` first
   - Check ad unit ID and network connectivity
   - Verify consent is granted if required

### Debug Mode

Use test ad unit IDs during development:

```kotlin
AdsConfig(
    application = this,
    appOpenAdUnitId = TestAdUnits.APP_OPEN,
    interstitialAdUnitId = TestAdUnits.INTERSTITIAL,
    bannerAdUnitId = TestAdUnits.BANNER,
    // ... other config
)
```

## Multi-Module Architecture

### Typical Setup

```
app/ (Host App)
├── build.gradle (implementation dependencies)
├── libs/
│   └── ads-module.aar
└── src/main/java/
    └── MyApplication.kt (AdsManager.initialize())

feature-module/ (Feature Module)
├── build.gradle (compileOnly dependencies)
├── libs/
│   └── ads-module.aar
└── src/main/java/
    └── FeatureActivity.kt (AdsManager.showInterstitial())
```

### Dependency Flow
1. **Feature Module**: Uses `compileOnly` to access AdsManager methods
2. **Host App**: Provides `implementation` dependencies and initializes AdsManager
3. **Runtime**: Feature module calls work because host app provides actual implementations

## License

This module is provided as-is for educational and development purposes.
