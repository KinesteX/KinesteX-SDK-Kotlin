# [KinesteX AI SDK](https://kinestex.com)
## INTEGRATE AI TRAINER IN YOUR APP IN MINUTES
### Easily transform your platform with our SDK: white-labeled workouts with precise motion tracking and real-time feedback tailored for accuracy and engagement


https://github.com/V-m1r/KinesteX-B2B-AI-Fitness-and-Physio/assets/62508191/ac4817ca-9257-402d-81db-74e95060b153

## Overview

Welcome to the documentation for the KinesteX SDK for Kotlin. This SDK allows you to integrate AI-powered fitness and physio workouts into your app with ease. The KinesteX SDK offers a variety of integration options, detailed user feedback, and customizable features to create an immersive fitness experience.

## Integration Options

| **Integration Option**     | **Description**                                                                                       | **Features**                                                                                                                                                      | **Details**                                                                                                             |
|----------------------------|-------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| **Complete User Experience** | Leave it to us to recommend the best workout routines for your customers, handle motion tracking, and overall user interface. High level of customization based on your brand book for a seamless experience. | - Long-term lifestyle workout plans <br> - Specific body parts and full-body workouts <br> - Individual exercise challenges (e.g., 20 squat challenge)              | [View Integration Options](https://www.figma.com/proto/XYEoV023iSFdhpw3w65zR1/Complete?page-id=0%3A1&node-id=0-1&viewport=793%2C330%2C0.1&t=d7VfZzKpLBsJAcP9-1&scaling=contain) |
| **Custom User Experience**   | Integrate the camera component with motion tracking. Real-time feedback on all customer movements. Control the position, size, and placement of the camera component. | - Real-time feedback on customer movements <br> - Communication of every repeat and mistake <br> - Customizable camera component position, size, and placement     | [View Details](https://www.figma.com/proto/JyPHuRKKbiQkwgiDTkGJgT/Camera-Component?page-id=0%3A1&node-id=1-4&viewport=925%2C409%2C0.22&t=3UccMcp1o3lKc0cP-1&scaling=contain) |

## Configuration

### Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.BLUETOOTH" />
```

### Project Setup

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
implementation("com.github.KinesteX:KinesteXSDKKotlin:1.0.8")
```

## Usage

### Initial Setup

1. **Prerequisites**: Ensure you’ve added the necessary permissions in `AndroidManifest.xml`.

2. **Launching the view**: To display KinesteX, we will be using WebView. To launch Complete UX call `createMainView` in KinesteXSDK:

   ```kotlin
   private var kinesteXWebView: WebView? = null

   @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)

       kinesteXWebView = KinesteXSDK.createMainView(
           this,
           apiKey, // store securely
           "YOUR COMPANY NAME",
           "YOUR USER ID",
           PlanCategory.Cardio,
           null, // class with user details
           isLoading,
           ::handleWebViewMessage // callback to handle messages
       )
   }
   ```

3. **Handling the data**: Use a ViewModel to handle changes:

   ```kotlin
   class ContentViewModel : ViewModel() {
       val showWebView = MutableLiveData<WebViewState>() 
       val selectedOptionPosition = MutableLiveData<Int>() // for the selected integration option (not necessary unless combining our solutions)
       val isLoading = MutableStateFlow(false) // loading state of the webview
       // CAMERA COMPONENT SPECIFIC
       val mistake = MutableStateFlow("") // for mistakes IF using our camera component
       val reps = MutableStateFlow(0) // for reps IF using our camera component
   }
   ```

### Creating Views

The KinesteX SDK provides multiple methods to create different views:

- **Main View (Complete User Experience)**:

   ```kotlin
   val webView = KinesteXSDK.createMainView(
       context,
       apiKey,
       companyName,
       userId,
       PlanCategory.Cardio,
       user,
       isLoading,
       onMessageReceived
   )
   ```

- **Plan View (Stand-alone workout plan page)**:

   ```kotlin
   val webView = KinesteXSDK.createPlanView(
       context,
       apiKey,
       companyName,
       userId,
       "Circuit Training",
       user,
       isLoading,
       onMessageReceived
   )
   ```

- **Workout View (Individual workout page)**:

   ```kotlin
   val webView = KinesteXSDK.createWorkoutView(
       context,
       apiKey,
       companyName,
       userId,
       "Fitness Lite",
       user,
       isLoading,
       onMessageReceived
   )
   ```

- **Challenge View**:

   ```kotlin
   val webView = KinesteXSDK.createChallengeView(
       context,
       apiKey,
       companyName,
       userId,
       "Jumping Jack",
       100,
       user,
       isLoading,
       onMessageReceived
   )
   ```

- **Camera Component (Just camera + our motion analysis and feedback)**:

   ```kotlin
   val webView = KinesteXSDK.createCameraComponent(
       context,
       apiKey,
       companyName,
       userId,
       listOf("Squats", "Jumping Jack"), // list of exercises we are tracking
       "Squats", // current exercise we are on (which one we start with first)
       user,
       isLoading,
       onMessageReceived // callback to handle messages including current rep count and mistakes
   )
   ```

### Handling Messages

The SDK sends various messages through the WebView. Implement a callback to handle these messages:

```kotlin
fun handleWebViewMessage(message: WebViewMessage) {
    when (message) {
        is WebViewMessage.ExitKinestex -> {
            viewModel.showWebView.postValue(WebViewState.ERROR) // to close the webview
        }
       
        // CAMERA COMPONENT SPECIFIC
        is WebViewMessage.Reps -> {
            message.data["value"]?.let { viewModel.reps.value = it } // to update the rep count from CAMERA COMPONENT
        }
        is WebViewMessage.Mistake -> {
            message.data["value"]?.let { viewModel.mistake.value = it }
        }
       
       
        else -> {
            // Handle other messages
        }
    }
}
```

### Updating the Current Exercise For Camera Component

Use the following method to update the current exercise in the camera component:

```kotlin
KinesteXSDK.updateCurrentExercise("Jumping Jack") // this exercise has to be from the list of exercises we are tracking
```

## Data Points

The KinesteX SDK provides various data points that are returned through the message callback. Here are the available data types:

| Type                       | Data                                                                                   | Description                                                                                               |
|----------------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `kinestex_launched`        | `dd mm yyyy hours:minutes:seconds`                                                      | When a user has launched KinesteX                                                                          |
| `exit_kinestex`            | `date: dd mm yyyy hours:minutes:seconds`, `time_spent: number`                          | Logs when a user clicks the exit button and the total time spent                                           |
| `plan_unlocked`            | `title: String, date: date and time`                                                    | Logs when a workout plan is unlocked by a user                                                            |
| `workout_opened`           | `title: String, date: date and time`                                                    | Logs when a workout is opened by a user                                                                   |
| `workout_started`          | `title: String, date: date and time`                                                    | Logs when a workout is started by a user                                                                  |
| `exercise_completed`       | `time_spent: number`, `repeats: number`, `calories: number`, `exercise: string`, `mistakes: [string: number]` | Logs each time a user finishes an exercise                                                                 |
| `total_active_seconds`     | `number`                                                                                | Logs every 5 seconds, counting the active seconds a user has spent working out                            |
| `left_camera_frame`        | `number`                                                                                | Indicates that a user has left the camera frame                                                           |
| `returned_camera_frame`    | `number`                                                                                | Indicates that a user has returned to the camera frame                                                    |
| `workout_overview`         | `workout: string`, `total_time_spent: number`, `total_repeats: number`, `total_calories: number`, `percentage_completed: number`, `total_mistakes: number` | Logs a complete summary of the workout                                                                    |
| `exercise_overview`        | `[exercise_completed]`                                                                 | Returns a log of all exercises and their data                                                             |
| `workout_completed`        | `workout: string`, `date: dd mm yyyy hours:minutes:seconds`                             | Logs when a user finishes the workout and exits the workout overview                                      |
| `active_days` (Coming soon)| `number`                                                                                | Represents the number of days a user has been opening KinesteX                                             |
| `total_workouts` (Coming soon)| `number`                                                                            | Represents the number of workouts a user has done since starting to use KinesteX                          |
| `workout_efficiency` (Coming soon)| `number`                                                                        | Represents the level of intensity with which a person has completed the workout                           |

For any questions, contact us at support@kinestex.com.

---

## License

The KinesteX SDK is licensed under the Apache License, Version 2.0. See the LICENSE file for more details.