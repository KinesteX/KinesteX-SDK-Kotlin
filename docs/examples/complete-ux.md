Complete code example for the `createMainView` function:
```kotlin
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kinestex.kinestexsdkkotlin.GenericWebView
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.PlanCategory
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), PermissionHandler {
    private lateinit var kinesteXWebView: GenericWebView
    private val isLoading = MutableStateFlow(false)

    // Plan category for personalized fitness goals
    private val planCategory = PlanCategory.Cardio

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // OPTIONAL: Custom Parameters
        val data = mutableMapOf<String, Any>()
        data["style"] = "light" // light or dark theme (customizable in admin dashboard)

        // Present the view and initialize it
        kinesteXWebView = KinesteXSDK.createMainView(
            this,
            planCategory,
            user = null, // UserDetails or null
            customParams = data, // example of using custom parameters. CAN BE NULL
            isLoading = isLoading,
            onMessageReceived = ::handleWebViewMessage,
            permissionHandler = this
        ) as GenericWebView

        // Now present kinesteXWebView fullscreen
        setContentView(kinesteXWebView)
    }

    private fun handleWebViewMessage(message: WebViewMessage) {
        when (message) {
            is WebViewMessage.ExitKinestex -> {
                // Dismiss main view
                finish()
            }
            else -> {
                println("Message received: $message")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        //  pass permission result to KinesteX webview
        kinesteXWebView.handlePermissionResult(isGranted)
    }

    // when request is sent, display system dialog for camera access
    override fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

}

```