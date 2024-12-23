### Key Features of Our Workout Plans

- **Personalized**: Tailored to height, weight, age, and activity level.
- **Goal-Oriented**: Supports strength, flexibility, and wellness goals.
- **Seamless Experience**: From recommendations to real-time feedback.
- **Customizable**: Brand-aligned app design.
- **Quick Integration**: Easy setup for advanced fitness solutions.

You can find workout plans in our library [here](https://workout-view.kinestex.com/), or create your own plans in our [admin portal](https://admin.kinestex.com).

# **PLAN Integration Example**

   ```kotlin
      // OPTIONAL: Custom Parameters
      val data = mutableMapOf<String, Any>()
      data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

     kinesteXWebView = KinesteXSDK.createPlanView(
                    this,
                    apiKey,
                    company,
                    userId,
                    "Circuit Training", // name or ID of the workout plan
                    userDetails, // userDetails or null
                    data, // custom parameters or null
                    viewModel.isLoading,
                    ::handleWebViewMessage, 
                    permissionHandler = this
     )  as GenericWebView?
   ```

# Next steps:
- ### [View handleWebViewMessage available data points](../../data.md)
- ### [View complete code example](../../examples/plans.md)
- ### [Explore more integration options](../overview.md)
