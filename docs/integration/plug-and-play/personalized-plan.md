### Key Features of Personalized Workout Plan

- **Personalized**: Tailored to height, weight, age, activity level, and fitness assessment.
- **Goal-Oriented**: Supports strength, flexibility, and wellness goals.
- **Seamless Experience**: From recommendations to real-time feedback.
- **Customizable**: Brand-aligned app design.
- **Quick Integration**: Easy setup for advanced fitness solutions.

# **PLAN Integration Example**

   ```kotlin
      // OPTIONAL: Custom Parameters
      val data = mutableMapOf<String, Any>()
      data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

     kinesteXWebView = KinesteXSDK.createPersonalizedPlanView(
                    this,
                    apiKey,
                    company,
                    userId,
                    userDetails, // userDetails or null
                    data, // custom parameters or null
                    viewModel.isLoading,
                    ::handleWebViewMessage, 
                    permissionHandler = this
     )  as GenericWebView?
   ```

# Next steps:
- ### [View handleWebViewMessage available data points](../../data.md)
- ### [Explore more integration options](../overview.md)
