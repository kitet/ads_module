# Ads Module

A plug-and-play advertising module for Android applications that provides a clean, encapsulated interface for managing ads.

## Usage Patterns

### 1. **Gradle Module Dependency (Recommended)**

When including the ads-module as a proper Gradle module dependency, you get full compile-time access to all classes and methods.

**In your app's `build.gradle`:**
```gradle
dependencies {
    implementation project(':ads-module')
    // ... other dependencies
}
```

**Direct usage in code:**
```kotlin
import mea.document.adsmodule.AdsManager
import mea.document.adsmodule.AdsConfig

// Direct instantiation - no reflection needed
val adsConfig = AdsConfig.Builder()
    .setAppId("your-app-id")
    .setTestMode(true)
    .build()

val adsManager = AdsManager.getInstance()
adsManager.initialize(adsConfig)
```

**Benefits:**
- ✅ Full compile-time access to all classes
- ✅ IntelliSense/autocomplete support
- ✅ Compile-time error checking
- ✅ Easy debugging and refactoring
- ✅ No reflection required

### 2. **AAR File Distribution (Current Setup)**

When distributing the ads-module as an AAR file, the consuming app cannot directly reference the classes at compile time, requiring reflection.

**In your app's `build.gradle`:**
```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    // ... other dependencies
}
```

**Reflection-based usage in code:**
```kotlin
// Use reflection to avoid direct dependency on ads module
val adsManagerClass = Class.forName("mea.document.adsmodule.AdsManager")
val initializeMethod = adsManagerClass.getMethod(
    "initialize", 
    Class.forName("mea.document.adsmodule.AdsConfig")
)

// Create AdsConfig using reflection with proper constructor
val adsConfigClass = Class.forName("mea.document.adsmodule.AdsConfig")
```

**Why reflection is necessary with AAR:**
- ❌ Classes are not available at compile time
- ❌ No IntelliSense/autocomplete
- ❌ Runtime errors instead of compile-time errors
- ❌ Harder to debug and maintain
- ❌ Required for AAR distribution

## Current Project Structure

```
AdsModule/
├── ads-module/           # The ads module source
├── app/                  # Demo app
│   └── libs/
│       └── ads-module-release.aar  # AAR distribution
└── settings.gradle       # Includes both modules
```

## Recommendations

### For Development/Debugging:
Use the Gradle module dependency approach:
```gradle
implementation project(':ads-module')
```

### For Production Distribution:
If you need to distribute as an AAR:
1. Document the reflection pattern clearly
2. Provide helper methods to reduce reflection boilerplate
3. Consider creating a wrapper library that handles reflection internally

### For Library Consumers:
If you're distributing this to other developers:
1. Provide both the source module and AAR options
2. Document both usage patterns
3. Include sample projects for each approach

## Dependencies

The ads-module encapsulates these dependencies internally:
- Google Play Services Ads
- User Messaging Platform
- AndroidX Lifecycle components
- Core Android libraries

## Migration Guide

To switch from AAR to module dependency:

1. **Remove AAR from libs folder:**
   ```bash
   rm app/libs/ads-module-release.aar
   ```

2. **Update app/build.gradle:**
   ```gradle
   dependencies {
       // Remove this:
       // implementation fileTree(dir: 'libs', include: ['*.aar'])
       
       // Add this:
       implementation project(':ads-module')
   }
   ```

3. **Update your code to remove reflection:**
   ```kotlin
   // Before (with reflection):
   val adsManagerClass = Class.forName("mea.document.adsmodule.AdsManager")
   
   // After (direct usage):
   import mea.document.adsmodule.AdsManager
   val adsManager = AdsManager.getInstance()
   ```

## Summary

- **The ads-module itself is fine** - it's properly structured and encapsulated
- **Reflection is only needed when consuming as an AAR file**
- **Use Gradle module dependency for development** to avoid reflection
- **AAR distribution is valid for production releases** but requires reflection in consuming apps
- **Choose the approach that fits your distribution needs**
