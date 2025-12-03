Complete code example for the `createCameraComponent` function:
```kotlin
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kinestex.kinestexsdkkotlin.GenericWebView
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CameraActivity : AppCompatActivity(), PermissionHandler {
    private lateinit var kinesteXWebView: GenericWebView
    private val isLoading = MutableStateFlow(false)

    // UI elements
    private lateinit var tvReps: TextView
    private lateinit var tvMistake: TextView

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Initialize UI elements
        tvReps = findViewById(R.id.tvReps)
        tvMistake = findViewById(R.id.tvMistake)

        // Create the camera component view
        kinesteXWebView = KinesteXSDK.createCameraComponent(
            context = this,
            currentExercise = "Squats",
            exercises = listOf("Squats", "Jumping Jack"),
            user = null,
            isLoading = isLoading,
            onMessageReceived = ::handleWebViewMessage,
            permissionHandler = this
        ) as GenericWebView

        // Add the camera component view to the layout
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(kinesteXWebView)
    }

    private fun handleWebViewMessage(message: WebViewMessage) {
        when (message) {
            is WebViewMessage.Reps -> {
                val reps = message.data["value"] as? Int
                tvReps.text = "Reps: $reps"
            }
            is WebViewMessage.Mistake -> {
                val mistake = message.data["value"] as? String
                tvMistake.text = "Mistake: $mistake"
            }
            else -> {
                // Handle other messages if needed
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