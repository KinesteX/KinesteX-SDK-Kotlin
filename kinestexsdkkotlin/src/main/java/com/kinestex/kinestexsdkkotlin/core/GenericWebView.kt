package com.kinestex.kinestexsdkkotlin.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("ViewConstructor")
class GenericWebView(
    context: Context,
    private val apiKey: String,
    private val companyName: String,
    private val userId: String,
    private val url: String,
    private val overlayColor: Int,
    private val onMessageReceived: (WebViewMessage) -> Unit,
    private val isLoading: MutableStateFlow<Boolean>,
    private val data: Map<String, Any>,
    private val permissionHandler: PermissionHandler
) : WebView(context) {

    companion object {
        private val logger = KinesteXLogger.instance

        @SuppressLint("StaticFieldLeak")
        private val controller = KinesteXWebViewController.getInstance()

        /**
         * Warms up WebView for faster first load
         * Pre-initializes WebView instance and loads base URL
         *
         * @param context Application context
         * @param apiKey API key for authentication
         * @param companyName Company identifier
         * @param userId User identifier
         */
        fun warmup(
            context: Context,
            apiKey: String? = null,
            companyName: String? = null,
            userId: String? = null
        ) {
            controller.warmup(context, apiKey, companyName, userId)
        }

        /**
         * Disposes the warmed-up WebView instance
         * Cleans up resources and releases memory
         */
        fun disposeWarmup() {
            controller.dispose()
        }

        /**
         * Check if WebView has been warmed up
         */
        val isWarmedUp: Boolean
            get() = controller.isWarmedUp()
    }

    private val overlayView: View = View(context).apply {
        setBackgroundColor(overlayColor)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    init {
        setupView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupView() {
        setBackgroundColor(Color.BLACK)

        // Load view using the singleton controller
        controller.loadView(
            context = context,
            apiKey = apiKey,
            companyName = companyName,
            userId = userId,
            url = url,
            data = data,
            permissionHandler = permissionHandler,
            onMessageReceived = { message ->
                if (message is WebViewMessage.KinestexLoaded) {
                    // IMPORTANT: UI changes must happen on main thread
                    // The callback is called from JavaBridge thread
                    Handler(Looper.getMainLooper()).postDelayed({
                        overlayView.visibility = View.GONE
                    }, 350)
                }
                onMessageReceived(message)
            },
            isLoading = isLoading
        )

        // Get the actual WebView from controller and add it as child
        controller.getWebView()?.let { webView ->
            // Remove from old parent if exists
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)

            // Add to this container
            addView(
                webView, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
        }

        // Add overlay on top and ensure it's visible for each new view
        overlayView.visibility = View.VISIBLE
        addView(overlayView)
    }

    /**
     * Method to be called from Activity's onRequestPermissionsResult
     */
    fun handlePermissionResult(granted: Boolean) {
        controller.handlePermissionResult(granted)
    }

    /**
     * Update current exercise
     */
    fun updateCurrentExercise(exercise: String) {
        controller.updateCurrentExercise(exercise)
    }

    /**
     * Send custom action
     */
    fun sendAction(action: String, value: String) {
        controller.sendAction(action, value)
    }

    /**
     * Can go back
     */
    override fun canGoBack(): Boolean = controller.canGoBack()

    /**
     * Go back
     */
    override fun goBack() {
        controller.goBack()
    }

    /**
     * Cleans up WebView resources
     * Note: With singleton controller, this doesn't destroy the WebView,
     * just cleans up references
     */
    fun cleanup() {
        try {
            logger.info("Cleaning up GenericWebView wrapper...")
            removeAllViews()
            logger.success("GenericWebView wrapper cleaned up")
        } catch (e: Exception) {
            logger.error("Error cleaning up GenericWebView wrapper", e)
        }
    }
}

fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    keys().forEach { key ->
        when (val value = this[key]) {
            is JSONObject -> map[key] = value.toMap()
            is JSONArray -> map[key] = value.toList()
            JSONObject.NULL -> map[key] = ""
            else -> map[key] = value
        }
    }
    return map
}

fun JSONArray.toList(): List<Any> = (0 until length()).map { i ->
    when (val value = this[i]) {
        is JSONObject -> value.toMap()
        is JSONArray -> value.toList()
        JSONObject.NULL -> ""
        else -> value
    }
}

