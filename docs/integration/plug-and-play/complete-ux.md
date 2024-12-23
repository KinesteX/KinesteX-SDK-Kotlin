# Compelete User Experience (createMainView): 
With this integration option we displays 3 best workout plans based on the provided category. The user can select one of the plans and start a long-term routine.

Available Categories to Sort Plans

| **Plan Category (key: planCategory)** |
|---------------------------------------|
| **Strength**                          |
| **Cardio**                            |
| **Weight Management**                 |
| **Rehabilitation**                    |
| **Custom**                            |

## Displaying the main view:
 ```kotlin
  
   @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
      
     // OPTIONAL: Custom Parameters
      val data = mutableMapOf<String, Any>()
      data["style"] = 'light' // light or dark theme (customizable in admin dashboard)

        // present the view and initialize it
       kinesteXWebView = KinesteXSDK.createMainView(
                    this,
                    apiKey,
                    company,
                    userId,
                    PlanCategory.Cardio,
                    userDetails, // UserDetails or null
                    customParams = data, // example of using custom parameters. CAN BE NULL
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this 
                )  as GenericWebView?

       
       // now present kinesteXWebView fullscreen
   }
   ```

# Next steps:
- ### [View handleWebViewMessage available data points](../../data.md)
- ### [View complete code example](../../examples/complete-ux.md)
- ### [Explore more integration options](../overview.md)
