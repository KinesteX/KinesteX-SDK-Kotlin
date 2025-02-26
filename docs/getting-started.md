### Minimum Device Requirements:

- iOS: iOS 14.0 or higher

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

Add the KinesteX SDK dependency in your app’s `build.gradle`:

```gradle
implementation("com.github.KinesteX:KinesteXSDKKotlin:1.2.0")
```

### 3. Request camera permission from app level:

Before a user starts a workout, we need to get access to camera **(if your app doesn't have access to camera already)**.
We are requesting camera permission on the app level and need to handle the result in the app level as well.

1.  Make sure your activity or fragment where you are presenting KinesteX views implements the `PermissionHandler` interface:
    `class MainActivity : AppCompatActivity(), PermissionHandler`

2.  Create necessary variables and override the `requestCameraPermission` method in `PermissionHandler` interface:

```kotlin
   // initialize the webview
   private var kinesteXWebView: GenericWebView? = null
   private var apiKey = yourApiKey // can be generated in admin dashboard
   private var companyName = yourCompanyName // also can be found in admin dashboard
   private var userId = yourUserId // can be any String, must be unique for each user
   
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

### 4. Handle data through callback function

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
