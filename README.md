# [KinesteX AI](https://kinestex.com)
## INTEGRATE AI TRAINER IN YOUR APP IN MINUTES
### Easily transform your platform with our SDK: white-labeled workouts with precise motion tracking and real-time feedback tailored for accuracy and engagement


https://github.com/V-m1r/KinesteX-B2B-AI-Fitness-and-Physio/assets/62508191/ac4817ca-9257-402d-81db-74e95060b153


## Configuration

#### Android.manifest

Add the following keys for camera usage:

```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.BLUETOOTH"/>
```

### Add the module to your project:
In project ->  `settings.gradle` or `build.gradle` (if settings.gradle is not your primary dependency resolution file)
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{ url = uri("https://jitpack.io") } // add jitpack 
    }
}
```
In app -> `build.gradle`
```gradle
implementation("com.github.V-m1r:KinesteXSDKKotlin:1.0.0")
```

### Available categories to sort workout plans: 

| **enum PlanCategory** | 
| --- | 
| **Strength** | 
| **Cardio** |
| **Rehabilitation** | 
| **WeightManagement** | 
| **Custom(String) - in case we release new custom plans for your usage** | 


### Available categories to sort workouts (displayed right below the plans): 

| **enum WorkoutCategory** | 
| --- | 
| **Fitness** |
| **Rehabilitation** |
| **Custom(String) - in case we release new custom workouts for your usage** | 

## Usage

### Initial Setup

1. **Prerequisites**:
    - Ensure you've added the necessary permissions in `Android.manifest`.
      
2. **Launching the view**:
   - To display KinesteX, call `createWebView` in KinesteXWebView:

   ```Kotlin
     private var kinesteXWebView: KinesteXWebView? = null

      @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
   
         kinesteXWebView = KinesteXWebView.createWebView(
            this,
            apiKey, // we recommend storing securely in database and accessing at runtime
            "YOUR COMPANY NAME",
            "YOUR USER ID",
            PlanCategory.Cardio,
            WorkoutCategory.Fitness, // WorkoutCategory.Custom("  ") // if you don't want to show the workout category block
            callback
        )

   }
   
   ```
3. **Handling the data**:
   We recommend creating a view model for handling changes: 
   ``` Kotlin
   class ContentViewModel : ViewModel() {
    val showWebView: MutableLiveData<String> = MutableLiveData(State.LOADING.name)

    fun handle(message: String) {

        try {
            val json = JSONObject(message)
            when (json.getString("type")) {
                "finished_workout" -> println("\nWorkout finished, data received: ${json.getString("data")} ")
                "error_occured" -> println("\nThere was an error: ${json.getString("data")} ")
                "exercise_completed" -> println("\nExercise completed: ${json.getString("data")} ")
                "exitApp" -> {

                    Log.e("TAG_viewmodel", "handle: " )
                    showWebView.postValue(State.ERROR.name)
                    println("\nUser closed workout window ")
                }
                "kinestex_launched" -> println("\nLaunched KinesteX: ${json.getString("data")} ")
                "workoutStarted" -> println("\nWorkout started: ${json.getString("data")} ")
                "workoutOpened" -> println("\nWorkout opened: ${json.getString("data")} ")
                "plan_unlocked" -> println("\nPlan unlocked: ${json.getString("data")} ")

                else -> { println("Type: ${json.getString("type")}, Data: ${json.getString("data")}") }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            println("Could not parse JSON message from WebView.")
        }
    }

   ```
   We pass you a callback function that sends all the data we receive from the user as a HTTPS POST Message: 
   ```Kotlin
    val callback = object : MessageCallback {
        override fun onMessageReceived(message: String) {
            viewModel.handle(message)
        }
    }

   ```

Available options are: 

```

    kinestexLaunched(String) - Logs when the KinesteX View is successfully launched.
    finishedWorkout(String) - Logs when a workout is finished.
    errorOccurred(String) - Logs when an error has occurred, example (user did not grant access to the camera)
    exerciseCompleted(String) - Logs when an exercise is completed.
    exitApp(String) - Logs when user clicks on exit button and wishes to close the KinesteX view.
    workoutOpened(String) - Logs when the workout description view is opened.
    workoutStarted(String) - Logs when a workout is started.
    planUnlocked(String) - Logs when a plan is unlocked.
    unknown(String) - For handling any unrecognized messages

```

Any questions? Contact us at support@kinestex.com
