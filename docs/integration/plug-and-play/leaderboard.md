# Ready-made Leaderboard: Boost User Engagement and Motivation

### Adaptive Design

The leaderboard automatically adapts to your KinesteX UI and can be fully customized in the admin dashboard.

### Real-time Updates

Whenever a new ranking is available, the leaderboard automatically refreshes to show the latest standings.

---

# **LEADERBOARD Integration Example**

   ```kotlin
      // OPTIONAL: Custom Parameters
      val data = mutableMapOf<String, Any>()
      data["style"] = 'light' // light or dark theme (customizable in admin dashboard)
      data["isHideHeaderMain"] = true // hide back button in leaderboard

    kinesteXWebView = KinesteXSDK.createLeaderboardView(
                    this, // context
                    "Squats", // name or ID of the exercise
                    username = "", // highlight username in leaderboard if known 
                    data, // custom parameters or null
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this 
    )  as GenericWebView?
   ```

# Next steps:

- ### [View onMessageReceived available data points](../../data.md)
- ### [Explore more integration options](../overview.md)
