package com.kinestex.kotlin_sdk

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.core.GenericWebView
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Example Activity demonstrating a native component (before/after photo picker)
 * injected on the KinesteX statistics page that scrolls with the web content.
 *
 * Mirrors the iOS approach:
 *   iOS:     scrollView.addSubview(view) at -height, contentInset.top = height
 *   Android: view in parent FrameLayout, translationY = -scrollY,
 *            CSS padding-top = height (equivalent to contentInset.top)
 *
 * Flow:
 *  1. WorkoutOverview                    -> store workout title & date
 *  2. WorkoutCompletionOverlayDismissed  -> show native photo picker component
 *  3. ExitKinestex                       -> remove component, finish activity
 */
class NativeHeaderExampleActivity : AppCompatActivity(), PermissionHandler {

    companion object {
        private const val TAG = "NativeHeaderExample"
    }

    // --- UI ---
    private lateinit var rootLayout: FrameLayout
    private var webView: GenericWebView? = null
    private var nativeComponent: View? = null
    private var beforeImageView: ImageView? = null
    private var afterImageView: ImageView? = null

    // --- State ---
    private val isLoading = MutableStateFlow(true)
    private var workoutTitle: String = ""
    private var workoutDate: String = ""

    // --- Permissions ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        webView?.handlePermissionResult(granted)
    }

    override fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        supportActionBar?.hide()

        rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }
        setContentView(rootLayout)

        createPlanWebView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            webView?.cleanup()
        }
    }

    // ---------------------------------------------------------------
    // WebView creation
    // ---------------------------------------------------------------

    private fun createPlanWebView() {
        val customParams = mutableMapOf<String, Any>(
            "nativeParentScroll" to true,
            "hideStatisticsHeader" to true
        )

        webView = KinesteXSDK.createPlanView(
            context = this,
            planName = "Circuit Training",
            user = null,
            style = null,
            customParams = customParams,
            isLoading = isLoading,
            onMessageReceived = ::handleMessage,
            permissionHandler = this
        ) as? GenericWebView

        webView?.let { wv ->
            wv.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            rootLayout.addView(wv)
        }
    }

    // ---------------------------------------------------------------
    // Message handling
    // ---------------------------------------------------------------

    private fun handleMessage(message: WebViewMessage) {
        Log.d(TAG, "Message: $message")

        when (message) {
            is WebViewMessage.WorkoutOverview -> {
                workoutTitle = message.data["title"] as? String ?: "Workout"
                workoutDate = message.data["date"] as? String ?: ""
                Log.d(TAG, "WorkoutOverview -> title=$workoutTitle date=$workoutDate")
            }

            is WebViewMessage.ExerciseOverview -> {
                Log.d(TAG, "ExerciseOverview -> ${message.data}")
            }

            is WebViewMessage.WorkoutCompletionOverlayDismissed -> {
                Log.d(TAG, "WorkoutCompletionOverlayDismissed -> showing native component")
                runOnUiThread { injectNativeComponent() }
            }

            is WebViewMessage.ExitKinestex -> {
                Log.d(TAG, "ExitKinestex -> cleaning up")
                runOnUiThread { removeNativeComponent() }
                finish()
            }

            else -> { /* other messages */ }
        }
    }

    // ---------------------------------------------------------------
    // Native component — before/after photo picker that scrolls with content
    //
    // Android equivalent of the iOS approach:
    //   iOS:     scrollView.addSubview(view) at -height, contentInset.top = height
    //   Android: view in parent FrameLayout, translationY driven by WebView scrollY,
    //            CSS padding-top on body = component height (≡ contentInset.top)
    // ---------------------------------------------------------------

    private fun injectNativeComponent() {
        if (nativeComponent != null) return

        val wv = KinesteXSDK.getWebView() ?: return

        // Build the photo picker component
        val component = buildPhotoPickerComponent()

        nativeComponent = component
        rootLayout.addView(component)

        // After layout pass, wire scroll tracking
        component.post {
            // Scroll to top so the component is visible
            wv.scrollTo(0, 0)

            // Track WebView scroll → translate component with content
            wv.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                component.translationY = -scrollY.toFloat()
            }
        }
    }

    private fun removeNativeComponent() {
        nativeComponent?.let { component ->
            rootLayout.removeView(component)

            KinesteXSDK.getWebView()?.setOnScrollChangeListener(null)
        }
        nativeComponent = null
        beforeImageView = null
        afterImageView = null
    }

    // ---------------------------------------------------------------
    // Photo picker UI
    // ---------------------------------------------------------------

    private fun buildPhotoPickerComponent(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.RED)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
        }

        // Title row
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titleTv = TextView(this).apply {
            text = workoutTitle
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
        }

        val doneBtn = TextView(this).apply {
            text = "Done"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                removeNativeComponent()
                finish()
            }
        }

        titleRow.addView(titleTv)
        titleRow.addView(doneBtn)

        // Before / After photo row
        val photoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        beforeImageView = buildImagePlaceholder("Before")
        afterImageView = buildImagePlaceholder("After")

        photoRow.addView(beforeImageView)
        photoRow.addView(afterImageView)

        container.addView(titleRow)
        container.addView(photoRow)

        return container
    }

    private fun buildImagePlaceholder(label: String): ImageView {
        val sizePx = dpToPx(120)

        val border = GradientDrawable().apply {
            setColor(Color.parseColor("#33FFFFFF")) // semi-transparent white
            setStroke(dpToPx(2), Color.WHITE)
            cornerRadius = dpToPx(8).toFloat()
        }

        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }
            background = border
            scaleType = ImageView.ScaleType.CENTER
            setImageResource(android.R.drawable.ic_menu_camera)
            setColorFilter(Color.WHITE)
            contentDescription = label
            setOnClickListener {
                Log.d(TAG, "$label photo tapped — hook up photo picker here")
            }
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setFullScreen() {
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
                return
            }
        }
        super.onBackPressed()
    }
}
