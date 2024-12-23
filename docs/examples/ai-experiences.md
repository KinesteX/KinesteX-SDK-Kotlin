Complete code example for the `createExperiencesView` function:
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
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ExperiencesActivity : AppCompatActivity(), PermissionHandler {
    private lateinit var kinesteXWebView: GenericWebView
    private val isLoading = MutableStateFlow(false)

    // Replace with your KinesteX credentials
    private val apiKey = "YOUR API KEY"
    private val company = "YOUR COMPANY NAME"
    private val userId = "YOUR USER ID"

    // Experience details
    private val experienceName = "box"
    private val experienceDuration = 100 // in seconds

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiences)

        // OPTIONAL: Custom Parameters
        val data = mutableMapOf<String, Any>()
        data["style"] = "light" // light or dark theme (customizable in admin dashboard)

        // Present the view and initialize it
        kinesteXWebView = KinesteXSDK.createExperiencesView(
            this,
            apiKey,
            company,
            userId,
            experienceName,
            experienceDuration,
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
                // Dismiss experiences view
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
        // Pass permission result to KinesteX webview
        kinesteXWebView.handlePermissionResult(isGranted)
    }

    // When request is sent, display system dialog for camera access
    override fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

```