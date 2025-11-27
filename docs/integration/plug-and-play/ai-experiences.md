### Immersive AI Experiences: Engage and Motivate

- **AI-Powered Fitness**: Interactive workouts driven by advanced motion analysis.
- **"Fight with a Shadow"**: Virtual punching bag reacts to hits, enhancing technique and engagement.
- **Real-Time Feedback**: Improve stance, punches, and form with AI guidance.

# **EXPERIENCE Integration Example**

```kotlin
     // OPTIONAL: Custom Parameters
     val data = mutableMapOf<String, Any>()
     data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

   kinesteXWebView = KinesteXSDK.createExperiencesView(
                   "box", // name of the experience (please contact support@kinestex.com for more details)
                   100, // duration of the experience
                   userDetails, // userDetails or null
                   data, // custom parameters or null
                   viewModel.isLoading,
                   ::handleWebViewMessage,
                   permissionHandler = this
   )  as GenericWebView?
```

# Next steps:

- ### [View handleWebViewMessage available data points](../../data.md)
- ### [View complete code example](../../examples/ai-experiences.md)
- ### [Explore more integration options](../overview.md)
