### Exciting Challenges: Drive Engagement and Motivation

- **Fun and Competitive**: Quick challenges with leaderboards for friendly competition.  
- **Boost Activity**: Keep fitness exciting and rewarding for users.  
- **Easy Integration**: Add dynamic challenges effortlessly to your app.  

You can find exercises in our library [here](https://workout-view.kinestex.com/?tab=exercises), or create your own exercises in our [admin portal](https://admin.kinestex.com).

# **CHALLENGE Integration Example**

   ```kotlin
      // OPTIONAL: Custom Parameters
      val data = mutableMapOf<String, Any>()
      data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

    kinesteXWebView = KinesteXSDK.createChallengeView(
                    this,
                    apiKey,
                    company,
                    userId,
                    "Squats", // name or ID of the exercise
                    100, // countdown of the challenge
                    userDetails, // userDetails or null
                    data, // custom parameters or null
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this 
    )  as GenericWebView?
   ```

# Next steps: 
- ### [View handleWebViewMessage available data points](../../data.md)
- ### [View complete code example](../../examples/challenge.md)
- ### [Explore more integration options](../overview.md)
