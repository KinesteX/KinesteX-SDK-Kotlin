# KinesteX SDK v2.0 - Quick Migration Guide

**The TL;DR:**
v2.0 introduces a **"Setup Once, Run Anywhere"** pattern. You initialize the SDK in your App class, and stop passing API keys to every single function.

---

### 🚀 How to Migrate in 3 Steps

#### 1. Initialize (Required)
Add this to your `Application` class. This warms up the WebView and validates credentials immediately.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Call this ONCE at startup
        KinesteXSDK.initialize(
            context = this,
            apiKey = "your-api-key",
            companyName = "YourCompany",
            userId = "user123"
        )
    }
}
```

#### 2. Update View Creation
You no longer need to pass `apiKey`, `companyName`, or `userId`.

**❌ Old Way (v1.x):**
```kotlin
val view = KinesteXSDK.createMainView(
    context, 
    "apiKey",     // DELETED
    "company",    // DELETED
    "userId",     // DELETED
    planCategory, 
    user, 
    ...
)
```

**✅ New Way (v2.0):**
```kotlin
val view = KinesteXSDK.createMainView(
    context, 
    planCategory, 
    user, 
    ...
)
```

#### 3. Update Data Fetching (Optional)
Use the new shared API client instance.

**✅ New Way:**
```kotlin
// Access the API directly without passing keys
val workouts = KinesteXSDK.api.fetchAPIContentData(
    contentType = ContentType.WORKOUT, 
    category = "Strength"
)
```

---

### 💡 What's New?

| Feature | Old (v1.x) | New (v2.0) |
| :--- | :--- | :--- |
| **Setup** | None | `initialize()` in App start |
| **Credentials** | Passed in every call | Stored automatically |
| **Performance** | Slower first load | **Instant load** (warmed up) |
| **Memory** | Prone to leaks | **Leak-proof** lifecycle |
| **Code Style** | Long parameter lists | Clean, short calls |

### 🗑️ Cleanup (Good Practice)
If you need to completely reset the SDK or free up resources:

```kotlin
KinesteXSDK.dispose()
```
