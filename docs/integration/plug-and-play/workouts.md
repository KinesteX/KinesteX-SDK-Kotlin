### Personalized Workouts: Anytime, Anywhere

- **Tailored for All Levels**: Workouts for strength, flexibility, or relaxation.
- **Time-Saving**: Quick, efficient sessions with zero hassle.
- **Engaging**: Keep users motivated with fresh, personalized routines.
- **Easy Integration**: Add workouts seamlessly with minimal effort.

You can find workout in our library [here](https://workout-view.kinestex.com/?tab=workouts), or create your own workouts in our [admin portal](https://admin.kinestex.com).

# **WORKOUT Integration Example**

```kotlin
   // OPTIONAL: Custom Parameters
   val data = mutableMapOf<String, Any>()
   data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

 kinesteXWebView = KinesteXSDK.createWorkoutView(
                 this,
                 apiKey,
                 company,
                 userId,
                 "Fitness Lite", // title or ID of the workout
                 userDetails, // userDetails or null
                 data, // custom parameters or null
                 isLoading = viewModel.isLoading,
                 onMessageReceived = ::handleWebViewMessage,
                 permissionHandler = this
)  as GenericWebView?
```

# Next steps:

- ### [View handleWebViewMessage available data points](../../data.md)
- ### [View complete code example](../../examples/workouts.md)
- ### [Explore more integration options](../overview.md)
