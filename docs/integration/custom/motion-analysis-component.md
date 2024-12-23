### KinesteX Motion Recognition: Real-Time Engagement

- **Interactive Tracking**: Advanced motion recognition for immersive fitness experiences.  
- **Real-Time Feedback**: Instantly track reps, spot mistakes, and calculate calories burned.  
- **Boost Motivation**: Keep users engaged with detailed exercise feedback.  
- **Custom Integration**: Adapt camera placement to fit your app’s design.  

## **CAMERA Integration Example**

### **1. Displaying the KinesteX Camera Component**

 ```kotlin
   kinesteXWebView = KinesteXSDK.createCameraComponent(
            context = context,
            apiKey = apiKey,
            companyName = company,
            userId = userId,
            currentExercise = "Squats", // current exercise name
            exercises = listOf("Squats", "Lunges"), // exercises that user is expected to do
            user = userDetails, // user details or null
            isLoading = viewModel.isLoading,
            onMessageReceived = ::handleWebViewMessage,
            permissionHandler = this
   )  as GenericWebView?
   ```

### **2. Updating the Current Exercise**
Easily update the exercise being tracked through a function:

```kotlin
KinesteXSDK.updateCurrentExercise("Lunges") // this exercise has to be from the list of exercises we are tracking
```

### **3. Handling Messages for Reps and Mistakes**
Track repetitions and identify mistakes made by users in real time:

```swift
```kotlin
    private fun handleWebViewMessage(message: WebViewMessage) {
    when (message) {
        is WebViewMessage.Reps -> {
            (message.data["value"] as? Int)?.let { viewModel.setReps(it) } // set value of reps
        }

        is WebViewMessage.Mistake -> {
            (message.data["value"] as? String)?.let {
                // set mistake 
                viewModel.setMistake(
                    it
                )
            }
        }

        else -> {
            // handle all other messages
            Log.d("Message received", message.toString())
        }
    }
}


```
# Next steps
### [View complete code example](../../examples/motion-analysis.md)