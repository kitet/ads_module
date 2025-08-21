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

#### For Host Apps (Final APK) - Complete Setup
The ads module provides a clean API facade, but the host app needs to provide runtime dependencies:

```gradle
dependencies {
    // Include the ads module AAR
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    
    // Required runtime dependencies
    implementation 'com.google.android.gms:play-services-ads:23.2.0'
    implementation 'com.google.android.ump:user-messaging-platform:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.6'
    implementation 'androidx.lifecycle:lifecycle-process:2.8.6'
}
```

#### For Feature Modules (compileOnly) - Clean API Access Only!
Feature modules only see your public API wrapper, never the underlying SDK:

```gradle
dependencies {
    // Feature modules only need API access at compile time
    compileOnly fileTree(dir: 'libs', include: ['*.aar'])
    // Runtime dependencies are provided by the host app
}
```

**Key Architecture**: 
- **Ads module**: Uses `implementation` to keep SDK dependencies internal
- **Feature modules**: Use `compileOnly` to access the clean API (`AdsManager.showInterstitialAd()`)
- **Host app**: Provides `implementation` for both AAR and runtime dependencies
- **Result**: Feature modules never directly touch SDK classes like `InterstitialAd` or `MobileAds`

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

### AdsManager - Clean API Facade

The `AdsManager` provides a simple, vendor-agnostic interface for ads functionality. 
All underlying SDK details are hidden internally, making it easy to swap ad vendors 
(AdMob, Meta, etc.) without breaking other modules.

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

#### Interstitial Ads - Clean API
```kotlin
// Preload for better performance
fun preloadInterstitial()

// Show with callbacks
fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null, onFailed: (() -> Unit)? = null): Boolean

// Simple one-liner for feature modules
fun showInterstitialAd(activity: Activity): Boolean

// Perfect for natural break points
fun showInterstitialAtBreakPoint(activity: Activity): Boolean
```

#### Banner Ads - Clean API
```kotlin
// Load with positioning
fun loadBanner(container: ViewGroup, isInline: Boolean)

// Inline banner (within content flow)
fun loadInlineBanner(container: ViewGroup)

// Anchored banner (bottom of screen)
fun loadAnchoredBanner(container: ViewGroup)

// Clean API with enum
fun loadBannerAd(container: ViewGroup, position: BannerPosition = BannerPosition.INLINE)
```

#### Status & Utilities
```kotlin
fun isReady(): Boolean
fun getStatus(): String
```

#### BannerPosition Enum
```kotlin
enum class BannerPosition {
    INLINE,    // Banner within content flow
    ANCHORED   // Banner anchored to screen edge
}
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

### Feature Module Usage (Clean API)

For feature modules that want simple, direct access without callbacks:

```kotlin
// Show interstitial - clean one-liner!
AdsManager.showInterstitialAd(activity)

// Show interstitial at natural break points
AdsManager.showInterstitialAtBreakPoint(activity)

// Load inline banner
AdsManager.loadInlineBanner(container)

// Load anchored banner at bottom
AdsManager.loadAnchoredBanner(container)

// Load banner with enum (cleaner API)
AdsManager.loadBannerAd(container, BannerPosition.INLINE)
AdsManager.loadBannerAd(container, BannerPosition.ANCHORED)

// Check if ads are ready
if (AdsManager.isReady()) {
    AdsManager.showInterstitialAd(activity)
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

The ads module works seamlessly with Jetpack Compose! Here are multiple ways to integrate:

#### 1. Simple Banner Ad Composable

```kotlin
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    position: BannerPosition = BannerPosition.INLINE
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { frame ->
            frame.post {
                AdsManager.loadBannerAd(frame, position)
            }
        }
    )
}
```

#### 2. Inline Banner Usage

```kotlin
@Composable
fun MyContentScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Your content
        Text("Welcome to our app!")
        
        // Inline banner within content flow
        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(16.dp)
        )
        
        // More content
        Text("Continue reading...")
    }
}
```

#### 3. Bottom Anchored Banner

```kotlin
@Composable
fun MainScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp) // Space for banner
        ) {
            Text("Your app content here")
            // ... more content
        }
        
        // Bottom anchored banner
        BannerAdView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp),
            position = BannerPosition.ANCHORED
        )
    }
}
```

#### 4. Interstitial Ads in Compose

```kotlin
@Composable
fun GameScreen(
    onLevelComplete: () -> Unit
) {
    val context = LocalContext.current
    
    Column {
        Button(
            onClick = {
                // Show interstitial before level completion
                AdsManager.showInterstitialAd(context as Activity)
                onLevelComplete()
            }
        ) {
            Text("Complete Level")
        }
        
        Button(
            onClick = {
                // Clean one-liner for premium features
                if (AdsManager.showInterstitialAd(context as Activity)) {
                    unlockPremiumFeature()
                }
            }
        ) {
            Text("Unlock Premium")
        }
        
        Button(
            onClick = {
                // Perfect for natural break points
                AdsManager.showInterstitialAtBreakPoint(context as Activity)
            }
        ) {
            Text("Show Ad at Break Point")
        }
    }
}
```

#### 5. Advanced Compose Integration with State

```kotlin
@Composable
fun AdsAwareScreen() {
    val context = LocalContext.current
    var showInterstitial by remember { mutableStateOf(false) }
    
    LaunchedEffect(showInterstitial) {
        if (showInterstitial) {
            AdsManager.showInterstitialSimple(context as Activity)
            showInterstitial = false
        }
    }
    
    Column {
        Button(
            onClick = { showInterstitial = true }
        ) {
            Text("Show Interstitial")
        }
        
        // Banner with loading state
        var bannerLoaded by remember { mutableStateOf(false) }
        
        if (!bannerLoaded) {
            CircularProgressIndicator()
        }
        
        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .onGloballyPositioned { 
                    bannerLoaded = true 
                }
        )
    }
}
```

#### 6. Compose with ViewModels

```kotlin
class GameViewModel : ViewModel() {
    fun onLevelComplete(activity: Activity) {
        // Show interstitial in ViewModel
        AdsManager.showInterstitialSimple(activity)
        // Continue with game logic
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    
    Column {
        Button(
            onClick = {
                viewModel.onLevelComplete(context as Activity)
            }
        ) {
            Text("Complete Level")
        }
        
        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}
```

#### 7. Compose Navigation Integration

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen()
        }
        composable("game") {
            GameScreen(
                onLevelComplete = {
                    // Show interstitial on navigation
                    AdsManager.showInterstitialSimple(context as Activity)
                    navController.navigate("levelComplete")
                }
            )
        }
        composable("levelComplete") {
            LevelCompleteScreen()
        }
    }
}
```

#### 8. Compose with Material 3

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3App() {
    val context = LocalContext.current
    
    Scaffold(
        bottomBar = {
            // Bottom anchored banner
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                isInline = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Your content
            Text("Material 3 App")
            
            Button(
                onClick = {
                    AdsManager.showInterstitialSimple(context as Activity)
                }
            ) {
                Text("Show Ad")
            }
        }
    }
}
```

#### 9. Compose Testing with Ads

```kotlin
@Composable
fun TestableScreen(
    onAdShown: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Button(
        onClick = {
            if (AdsManager.showInterstitialSimple(context as Activity)) {
                onAdShown()
            }
        }
    ) {
        Text("Test Interstitial")
    }
}

// In your tests
@Test
fun testInterstitialShown() {
    var adShown = false
    
    composeTestRule.setContent {
        TestableScreen(
            onAdShown = { adShown = true }
        )
    }
    
    composeTestRule.onNodeWithText("Test Interstitial").performClick()
    // Verify ad was shown
}
```

#### 10. Compose Best Practices

```kotlin
// ✅ Good: Check if ads are ready
@Composable
fun SafeAdScreen() {
    val context = LocalContext.current
    
    if (AdsManager.isReady()) {
        Button(
            onClick = {
                AdsManager.showInterstitialSimple(context as Activity)
            }
        ) {
            Text("Show Ad")
        }
    }
}

// ✅ Good: Use remember for expensive operations
@Composable
fun OptimizedBanner() {
    val bannerKey = remember { UUID.randomUUID().toString() }
    
    BannerAdView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        key = bannerKey // Prevents unnecessary recompositions
    )
}

// ✅ Good: Handle ad states
@Composable
fun AdWithState() {
    var adState by remember { mutableStateOf("idle") }
    val context = LocalContext.current
    
    Column {
        when (adState) {
            "loading" -> CircularProgressIndicator()
            "ready" -> {
                Button(
                    onClick = {
                        adState = "showing"
                        AdsManager.showInterstitialSimple(context as Activity)
                    }
                ) {
                    Text("Show Ad")
                }
            }
            "showing" -> Text("Ad is showing...")
        }
    }
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
   - **Host App**: Must include both the AAR (`implementation`) and runtime dependencies (`implementation`)
   - **Feature Module**: Use `compileOnly` - you only need API access at compile time
   - **Root Cause**: The AAR doesn't include transitive dependencies, host app must provide them

2. **Module can't find AdsManager methods**
   - Use `compileOnly` dependency in feature modules for API access
   - Host app uses `implementation` for both AAR and runtime dependencies
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

## Multi-Module Architecture & Implementation Details

### Typical Setup

```
app/ (Host App)
├── build.gradle (implementation AAR + runtime dependencies)
├── libs/
│   └── ads-module.aar
└── src/main/java/
    └── MyApplication.kt (AdsManager.initialize())

feature-module/ (Feature Module)
├── build.gradle (compileOnly AAR only)
├── libs/
│   └── ads-module.aar
└── src/main/java/
    └── FeatureActivity.kt (AdsManager.showInterstitialAd())

ads-module/ (Library Module)
├── build.gradle (implementation SDK dependencies)
└── src/main/java/
    └── AdsManager.kt (Clean API facade)
```

### Complete Implementation Example

#### 1. Host App `build.gradle`
```gradle
android {
    namespace 'com.yourcompany.yourapp'
    compileSdk 36
    // ... other config
}

dependencies {
    // Standard dependencies
    implementation 'androidx.core:core-ktx:1.15.0'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    
    // Ads module with runtime dependencies
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    implementation 'com.google.android.gms:play-services-ads:23.2.0'
    implementation 'com.google.android.ump:user-messaging-platform:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.6'
    implementation 'androidx.lifecycle:lifecycle-process:2.8.6'
}
```

#### 2. Feature Module `build.gradle`
```gradle
android {
    namespace 'com.yourcompany.feature.shopping'
    compileSdk 36
    // ... other config
}

dependencies {
    // Standard dependencies
    implementation 'androidx.core:core-ktx:1.15.0'
    implementation 'androidx.fragment:fragment-ktx:1.8.5'
    
    // Ads module API access only
    compileOnly fileTree(dir: 'libs', include: ['*.aar'])
    // No need for AdMob dependencies - host app provides them!
}
```

#### 3. Host App `Application.kt`
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ads module once in host app
        AdsManager.initialize(
            AdsConfig(
                application = this,
                appOpenAdUnitId = "ca-app-pub-xxxxxxxx/1234567890",
                interstitialAdUnitId = "ca-app-pub-xxxxxxxx/0987654321",
                bannerAdUnitId = "ca-app-pub-xxxxxxxx/1122334455",
                enableAppOpenAds = true,
                delayFirstAppOpenMs = 3000L
            )
        )
    }
}
```

#### 4. Feature Module Usage
```kotlin
class ShoppingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)
        
        // Feature module uses clean API - no AdMob imports needed!
        findViewById<Button>(R.id.btn_checkout).setOnClickListener {
            // Show ad at natural break point
            AdsManager.showInterstitialAtBreakPoint(this)
            proceedToCheckout()
        }
        
        // Load banner in shopping list
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        bannerContainer.post {
            AdsManager.loadInlineBanner(bannerContainer)
        }
    }
}
```

### Dependency Flow
1. **Ads Module**: Uses `implementation` to keep SDK dependencies internal to the module
2. **Feature Module**: Uses `compileOnly` to access AdsManager API methods at compile time
3. **Host App**: Uses `implementation` for both the AAR and required runtime dependencies
4. **Runtime**: Feature module calls work because host app provides the actual SDK implementations
5. **Architecture**: Feature modules only see the clean API facade, never the underlying SDK classes

### Benefits & Trade-offs

#### ✅ **Benefits of This Architecture**

**For Feature Modules:**
- **Clean API**: Only see `AdsManager.showInterstitialAd()`, never `InterstitialAd.load()`
- **Vendor Agnostic**: Easy to swap from AdMob to Meta/Unity without code changes
- **Lightweight**: `compileOnly` means no dependency bloat
- **Future-proof**: Changes to SDK don't affect feature modules

**For Host Apps:**
- **Single Source of Truth**: All ads logic centralized in one module
- **Control**: Host app controls SDK versions and initialization
- **Flexibility**: Can disable ads entirely by not initializing
- **Testing**: Easy to mock `AdsManager` for unit tests

**For Ads Module:**
- **Encapsulation**: SDK details hidden behind clean facade
- **Maintainability**: Changes don't ripple to consumers
- **Swappable**: Easy to replace AdMob with other ad networks
- **Policy Compliance**: Centralized ad behavior following best practices

#### ⚠️ **Trade-offs**

**Host App Responsibility:**
- Must include runtime dependencies (but this is explicit and clear)
- Must initialize the ads module (but this ensures proper setup)
- Must manage SDK versions (but this prevents conflicts)

**Not a Fat AAR:**
- AAR doesn't include transitive dependencies (but this is intentional for cleaner architecture)
- Requires explicit dependency management (but this prevents version conflicts)

#### 🎯 **Why This Approach is Superior**

1. **Clean Architecture**: Clear separation between API and implementation
2. **Vendor Independence**: Easy to swap ad providers without breaking changes
3. **Explicit Dependencies**: Host app knows exactly what it's including
4. **No Conflicts**: Each app controls its own SDK versions
5. **Testable**: Easy to mock and test ads behavior
6. **Policy Compliant**: Centralized ads behavior following Google's guidelines

### Dependency Architecture Diagram
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Feature Module │    │   Ads Module    │    │    Host App     │
│                 │    │                 │    │                 │
│ compileOnly AAR │───▶│ implementation  │    │ implementation  │
│                 │    │ - AdMob SDK     │    │ - AAR           │
│ AdsManager.xxx()│    │ - UMP SDK       │    │ - AdMob SDK     │
│                 │    │ - Lifecycle     │    │ - UMP SDK       │
│                 │    │                 │    │ - Lifecycle     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                │
                        ┌───────▼────────┐
                        │   Runtime       │
                        │ Feature calls   │
                        │ work because    │
                        │ host provides   │
                        │ implementations │
                        └────────────────┘
```

## License

This module is provided as-is for educational and development purposes.
