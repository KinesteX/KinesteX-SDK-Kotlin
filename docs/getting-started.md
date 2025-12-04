### Minimum Device Requirements:

- Android 9 or higher

## Configuration

### 1. Add Permissions

#### AndroidManifest.xml

Add the following keys for camera usage:

```xml
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<!-- Optional: To detect device orientation when prompting to position phone correctly-->
<uses-feature android:name="android.hardware.sensor.accelerometer" android:required="false" />
<uses-feature android:name="android.hardware.sensor.gyroscope" android:required="false" />
```

### 2. Add jitpack and Install KinesteX SDK framework

Add the JitPack repository to your project’s `settings.gradle` or `build.gradle` (if `settings.gradle` is not your primary dependency resolution file):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the KinesteX SDK dependency in your app's `build.gradle`:

```gradle
implementation("com.github.KinesteX:KinesteX-SDK-Kotlin:2.0.0")
```

### 3. Initialize the SDK (v2.0 - Recommended)

Initialize the SDK once in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize KinesteX SDK once at app start
        KinesteXSDK.initialize(
            context = this,
            apiKey = "your-api-key",        // can be generated in admin dashboard
            companyName = "your-company",   // can be found in admin dashboard
            userId = "unique-user-id"       // must be unique for each user
        )
    }
}
```

Register your custom Application class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

**Note:** This initialization is required for v2.0 APIs. If you're still using v1.x code patterns, initialization is optional but recommended for better performance (WebView warmup).

### 4. Request camera permission from app level:

Before a user starts a workout, we need to get access to camera **(if your app doesn't have access to camera already)**.
We are requesting camera permission on the app level and need to handle the result in the app level as well.

1.  Make sure your activity or fragment where you are presenting KinesteX views implements the `PermissionHandler` interface:
    `class MainActivity : AppCompatActivity(), PermissionHandler`

2.  Create necessary variables and override the `requestCameraPermission` method in `PermissionHandler` interface:

```kotlin
   // initialize the webview
   private var kinesteXWebView: GenericWebView? = null

   // v2.0: No need to declare apiKey, companyName, userId here if you initialized the SDK
   // v1.x (legacy): Uncomment below if not using SDK initialization
   // private var apiKey = yourApiKey
   // private var companyName = yourCompanyName
   // private var userId = yourUserId

   // OPTIONAL: 
  // If available, you can pass user details to automatically adjust some exercises and correctly estimate calories based on the user's information.
  // Note: User details are only used on the device for customization purposes during the session. If you want to persist the customization across sessions, you'll need to pass the customization data everytime when launching KinesteX.
   private var userDetails = UserDetails(age = 20, height = 170, weight = 180, gender = Gender.MALE, lifestyle = Lifestyle.ACTIVE)

    // request camera permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
    //  pass permission result to KinesteX webview
        kinestexWebView?.handlePermissionResult(isGranted)
    }

   // when request is sent, display system dialog for camera access
   override fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
   }
```

### 5. Handle data through callback function

The SDK sends various messages. Implement a callback to handle these messages:

```kotlin
    private fun handleWebViewMessage(message: WebViewMessage) {
    when (message) {
        is WebViewMessage.ExitKinestex -> {
          // dismiss KinesteX view since user is clicking on exit button inside the view
        }

        // handle other messages
        else -> {
            Log.d("Message received", message.toString())
        }
    }
}

```

# Next Steps

### **[> Available Integration Options](integration/overview.md)**
### **[> Learn more about data handling](data.md)**
