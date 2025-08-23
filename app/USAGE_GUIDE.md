# Ads Module Usage Guide

## Current Setup: AAR Distribution

Your app is currently configured to use the ads-module as an AAR file. This approach requires reflection because the classes are not available at compile time.

### Why Reflection is Required

When using an AAR file:
- Classes are only available at runtime
- No compile-time access to `mea.document.adsmodule.*` classes
- Direct imports will cause compilation errors
- Reflection is the only way to access the classes

### Current Configuration

**app/build.gradle:**
```gradle
dependencies {
    // AAR distribution - requires reflection
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    
    // Runtime dependencies for ads module
    implementation libs.play.services.ads
    implementation libs.user.messaging.platform
    implementation libs.androidx.lifecycle.runtime.ktx
    implementation libs.androidx.lifecycle.process
}
```

**Required reflection code:**
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

## Alternative: Module Dependency (No Reflection)

To eliminate reflection and get full compile-time access, switch to using the ads-module as a Gradle dependency.

### Step 1: Update Dependencies

**app/build.gradle:**
```gradle
dependencies {
    // Remove AAR approach:
    // implementation fileTree(dir: 'libs', include: ['*.aar'])
    
    // Add module dependency:
    implementation project(':ads-module')
    
    // Runtime dependencies (still needed)
    implementation libs.play.services.ads
    implementation libs.user.messaging.platform
    implementation libs.androidx.lifecycle.runtime.ktx
    implementation libs.androidx.lifecycle.process
}
```

### Step 2: Remove AAR File

```bash
rm app/libs/ads-module-release.aar
```

### Step 3: Update Your Code

**Before (with reflection):**
```kotlin
// Reflection-based initialization
val adsManagerClass = Class.forName("mea.document.adsmodule.AdsManager")
val initializeMethod = adsManagerClass.getMethod(
    "initialize", 
    Class.forName("mea.document.adsmodule.AdsConfig")
)

// Create AdsConfig using reflection
val adsConfigClass = Class.forName("mea.document.adsmodule.AdsConfig")
```

**After (direct usage):**
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

## When to Use Each Approach

### Use AAR + Reflection When:
- Distributing the ads-module as a standalone library
- Other developers need to include it without source access
- You want to hide the internal implementation details
- You're building a commercial library

### Use Module Dependency When:
- Developing and debugging the ads-module
- You have access to the source code
- You want compile-time safety and IntelliSense
- You're building an internal library for your team

## Benefits of Module Dependency

✅ **Compile-time Safety**: Errors caught during compilation
✅ **IntelliSense**: Full autocomplete and documentation
✅ **Easy Debugging**: Direct access to source code
✅ **No Reflection**: Clean, maintainable code
✅ **Better Performance**: No runtime reflection overhead

## Benefits of AAR Distribution

✅ **Encapsulation**: Internal dependencies are hidden
✅ **Distribution**: Easy to share as a compiled library
✅ **Version Control**: Can version the library independently
✅ **Binary Size**: Smaller than including source code

## Summary

**The ads-module is working correctly** - the reflection requirement is due to how it's being consumed (AAR file), not a problem with the module itself.

- **Current setup**: AAR + Reflection (required for distribution)
- **Alternative**: Module dependency (no reflection, better development experience)
- **Choose based on**: Your distribution needs vs. development experience preferences

Both approaches are valid and serve different use cases!
