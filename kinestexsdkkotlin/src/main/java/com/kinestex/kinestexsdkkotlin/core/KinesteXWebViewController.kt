package com.kinestex.kinestexsdkkotlin.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.*
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

class KinesteXWebViewController private constructor() {

    private val logger = KinesteXLogger.instance

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: KinesteXWebViewController? = null

        fun getInstance(): KinesteXWebViewController {
            return instance ?: synchronized(this) {
                instance ?: KinesteXWebViewController().also { instance = it }
            }
        }

        private const val WARMUP_URL = "https://ai.kinestex.com/warmup"
        private const val MAX_RETRIES = 3
    }

    // WebView state
    private var webView: WebView? = null
    private var isInitialized = false
    private var isFirstViewLoad = true

    // Current state
    private var currentContext: WeakReference<Context>? = null
    private var currentUrl: String? = null
    private var currentApiKey: String? = null
    private var currentCompanyName: String? = null
    private var currentUserId: String? = null
    private var currentData: Map<String, Any>? = null
    private var currentPermissionHandler: PermissionHandler? = null

    // Callbacks
    private var onMessageReceived: ((WebViewMessage) -> Unit)? = null
    private var isLoading: MutableStateFlow<Boolean>? = null

    // Retry mechanism
    private var launchTimer: Handler? = null
    private var retryCount = 0

    // Permission handling
    private var pendingCameraPermissionRequest: PermissionRequest? = null

    /**
     * Check if WebView has been warmed up
     */
    fun isWarmedUp(): Boolean = isInitialized

    /**
     * Get the current WebView instance
     */
    fun getWebView(): WebView? = webView

    /**
     * Warmup the WebView
     * Pre-loads a WebView instance for faster first load
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun warmup(context: Context, apiKey: String? = null, companyName: String? = null, userId: String? = null) {
        if (isInitialized && webView != null) {
            logger.info("WebView already warmed up")
            return
        }

        logger.info("Starting WebView warmup...")

        // Store credentials if provided
        if (apiKey != null && companyName != null && userId != null) {
            currentApiKey = apiKey
            currentCompanyName = companyName
            currentUserId = userId
            currentUrl = WARMUP_URL
            logger.info("Credentials stored during warmup")
        }

        currentContext = WeakReference(context.applicationContext)

        // Create WebView
        webView = WebView(context.applicationContext).apply {
            setupWebView(this)
            loadUrl(WARMUP_URL)
        }

        isInitialized = true
        logger.success("WebView warmup completed")
    }

    /**
     * Load a view using the existing WebView
     * Navigates to the new URL instead of creating a new WebView
     */
    fun loadView(
        context: Context,
        apiKey: String,
        companyName: String,
        userId: String,
        url: String,
        data: Map<String, Any>,
        permissionHandler: PermissionHandler,
        onMessageReceived: (WebViewMessage) -> Unit,
        isLoading: MutableStateFlow<Boolean>
    ) {
        if (!isInitialized || webView == null) {
            logger.error("WebView is not warmed up yet!")
            throw IllegalStateException("WebView is not warmed up. Call warmup() first.")
        }

        logger.info("Loading view: $url")

        // Store current state
        currentContext = WeakReference(context)
        currentUrl = url
        currentApiKey = apiKey
        currentCompanyName = companyName
        currentUserId = userId
        currentData = data
        currentPermissionHandler = permissionHandler
        this.onMessageReceived = onMessageReceived
        this.isLoading = isLoading

        // Reset first view load flag to ensure history gets cleared for this new view
        isFirstViewLoad = true

        // Set loading state
        isLoading.value = true

        // Navigate to new URL
        navigateToUrl(url)
    }

    /**
     * Navigate to a new URL in the existing WebView
     */
    private fun navigateToUrl(url: String) {
        try {
            logger.info("Navigating to: $url")

            // Clear previous content to prevent showing old URL during transition
            webView?.loadUrl("about:blank")

            // Load the target URL after a brief delay to ensure blank page is loaded
            Handler(Looper.getMainLooper()).postDelayed({
                webView?.loadUrl(url)
            }, 50)
        } catch (e: Exception) {
            logger.error("Navigation failed", e)
            isLoading?.value = false
        }
    }

    /**
     * Setup WebView with all necessary configurations
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            setBackgroundColor(Color.BLACK)

            // WebChromeClient for permissions
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    handlePermissionRequest(request)
                }
            }

            // WebViewClient for page events
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isLoading?.value = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    val loadedUrl = url

                    // If this was the warmup load
                    if (loadedUrl == WARMUP_URL) {
                        logger.success("Warmup page loaded")
                        // If we have a different target URL, navigate to it
                        if (currentUrl != null && currentUrl != WARMUP_URL) {
                            logger.info("Navigating to target URL: $currentUrl")
                            navigateToUrl(currentUrl!!)
                        }
                    } else if (loadedUrl != "about:blank") {
                        logger.info("Page loaded: $loadedUrl")

                        // Clear navigation history when loading a new view to remove about:blank
                        // This ensures back button works correctly (no blank page in history)
                        // Match URLs flexibly to handle hash navigation and redirects
                        val urlsMatch = loadedUrl == currentUrl ||
                                       loadedUrl?.startsWith(currentUrl ?: "") == true ||
                                       loadedUrl?.substringBefore("#")?.substringBefore("?") ==
                                       currentUrl?.substringBefore("#")?.substringBefore("?")

                        if (urlsMatch && isFirstViewLoad) {
                            logger.info("New view loaded, clearing navigation history")
                            try {
                                view?.clearHistory()
                                isFirstViewLoad = false
                                logger.success("Navigation history cleared - blank page removed")
                            } catch (e: Exception) {
                                logger.error("Failed to clear navigation history", e)
                            }
                        }
                    }
                }
            }

            // Add JavaScript interface
            addJavascriptInterface(MessageHandler(), "messageHandler")
        }
    }

    /**
     * Handle permission requests from WebView
     */
    private fun handlePermissionRequest(request: PermissionRequest) {
        Log.d("KinesteX SDK", "Permission request received: ${request.resources.contentToString()}")

        try {
            if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                pendingCameraPermissionRequest = request
                currentPermissionHandler?.requestCameraPermission()
            } else {
                request.grant(request.resources)
            }
        } catch (e: Exception) {
            Log.e("KinesteX SDK", "Error handling permission request", e)
            request.grant(request.resources)
        }
    }

    /**
     * Handle camera permission result
     */
    fun handlePermissionResult(granted: Boolean) {
        pendingCameraPermissionRequest?.let { request ->
            try {
                if (granted) {
                    request.grant(request.resources)
                } else {
                    request.deny()
                }
            } catch (e: Exception) {
                Log.e("KinesteX SDK", "Error handling permission result", e)
                request.deny()
            } finally {
                pendingCameraPermissionRequest = null
            }
        }
    }

    /**
     * Convert Kotlin value to JSON-compatible value
     * Properly handles Lists, Maps, and primitive types
     */
    private fun toJsonValue(value: Any?): Any {
        return when (value) {
            null -> JSONObject.NULL
            is List<*> -> {
                JSONArray().apply {
                    value.forEach { item ->
                        put(toJsonValue(item))
                    }
                }
            }
            is Map<*, *> -> {
                JSONObject().apply {
                    value.forEach { (k, v) ->
                        put(k.toString(), toJsonValue(v))
                    }
                }
            }
            is String, is Number, is Boolean -> value
            else -> value.toString()
        }
    }

    /**
     * Load initial data into the WebView
     */
    private fun loadInitialData() {
        if (webView == null || currentApiKey == null || currentCompanyName == null ||
            currentUserId == null || currentUrl == null) {
            logger.error("Cannot load initial data - missing required state")
            return
        }

        logger.info("Loading initial data")

        val message = JSONObject().apply {
            put("key", currentApiKey)
            put("company", currentCompanyName)
            put("userId", currentUserId)
            put("exercises", JSONArray(currentData?.get("exercises") as? List<String> ?: emptyList<String>()))
            put("currentExercise", currentData?.get("currentExercise") as? String ?: "")

            // Add other data fields
            currentData?.forEach { (key, value) ->
                if (key !in listOf("exercises", "currentExercise")) {
                    put(key, toJsonValue(value))
                }
            }
        }

        val script = """
            (function() {
                setTimeout(function() {
                    var event = new MessageEvent('message', {
                        data: $message,
                        origin: '$currentUrl',
                        source: window
                    });
                    window.dispatchEvent(event);
                }, 100);
            })();
        """.trimIndent()

        logger.info("Script: $script")

        try {
            webView?.evaluateJavascript(script) { result ->
                Log.d("WebView", "Initial data evaluated: $result")
            }

            // Set up retry mechanism
            launchTimer?.removeCallbacksAndMessages(null)
            launchTimer = Handler(Looper.getMainLooper()).apply {
                postDelayed({
                    if (retryCount < MAX_RETRIES) {
                        retryCount++
                        logger.error("Initial data not received within 1 second. Resending. Attempt $retryCount")
                        loadInitialData()
                    }
                }, 1000)
            }
        } catch (e: Exception) {
            logger.error("Error sending message", e)
        }
    }

    /**
     * Update current exercise dynamically
     * MUST run on main thread
     */
    fun updateCurrentExercise(exercise: String) {
        if (webView == null || currentUrl == null) {
            logger.error("Cannot update exercise - WebView not ready")
            return
        }

        logger.info("Updating currentExercise: $exercise")

        // IMPORTANT: Run on main thread
        Handler(Looper.getMainLooper()).post {
            val script = """
                (function() {
                    var event = new MessageEvent('message', {
                        data: { currentExercise: '$exercise' },
                        origin: '$currentUrl',
                        source: window
                    });
                    window.dispatchEvent(event);
                })();
            """.trimIndent()

            try {
                webView?.evaluateJavascript(script) { result ->
                    Log.d("WebView", "Updated current exercise: $result")
                }
            } catch (e: Exception) {
                logger.error("Failed to update exercise", e)
            }
        }
    }

    /**
     * Send a custom action message to the WebView
     * MUST run on main thread
     */
    fun sendAction(action: String, value: String) {
        if (webView == null || currentUrl == null) {
            logger.error("Cannot send action - WebView not ready")
            return
        }

        if (action.isEmpty() || value.isEmpty()) {
            logger.error("Action and value are required")
            return
        }

        logger.info("Sending action: $action = $value")

        // IMPORTANT: Run on main thread
        Handler(Looper.getMainLooper()).post {
            val messagePayload = JSONObject().apply {
                put(action, value)
            }

            val script = """
                (function() {
                    window.postMessage($messagePayload, '$currentUrl');
                })();
            """.trimIndent()

            try {
                webView?.evaluateJavascript(script) { result ->
                    logger.info("Action sent successfully")
                }
            } catch (e: Exception) {
                logger.error("Failed to send action", e)
            }
        }
    }

    /**
     * Handle back button press
     */
    fun canGoBack(): Boolean = webView?.canGoBack() ?: false

    fun goBack() {
        Handler(Looper.getMainLooper()).post {
            webView?.goBack()
        }
    }

    /**
     * Cleanup and dispose
     */
    fun dispose() {
        logger.info("Disposing WebView controller...")

        launchTimer?.removeCallbacksAndMessages(null)
        launchTimer = null

        webView?.let { view ->
            try {
                view.stopLoading()
                view.loadUrl("about:blank")
                view.clearCache(true)
                view.clearHistory()
                view.removeJavascriptInterface("messageHandler")
                view.destroy()
            } catch (e: Exception) {
                logger.error("Error disposing WebView", e)
            }
        }

        // Clear state
        webView = null
        isInitialized = false
        isFirstViewLoad = true
        currentContext = null
        currentUrl = null
        currentApiKey = null
        currentCompanyName = null
        currentUserId = null
        currentData = null
        currentPermissionHandler = null
        onMessageReceived = null
        isLoading = null
        pendingCameraPermissionRequest = null
        retryCount = 0

        logger.success("WebView controller disposed")
    }

    /**
     * JavaScript interface for handling messages from WebView
     * This runs on JavaBridge thread, so we must post to main thread for WebView operations
     */
    private inner class MessageHandler {
        @JavascriptInterface
        fun postMessage(message: String) {
            try {
                val json = JSONObject(message)
                val type = json.optString("type")
                val dataMap = json.toMap()

                logger.info("Received message from WebView: $type")

                // Handle special message types
                when (type) {
                    "kinestex_launched" -> {
                        logger.info("KinesteX launched - canceling retry timer")
                        // Run on main thread
                        Handler(Looper.getMainLooper()).post {
                            launchTimer?.removeCallbacksAndMessages(null)
                            retryCount = 0
                        }
                    }

                    "kinestex_loaded" -> {
                        logger.success("KinesteX loaded - sending initial data")
                        // Update loading state on main thread after delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            isLoading?.value = false
                        }, 200)

                        // IMPORTANT: Load initial data on main thread
                        Handler(Looper.getMainLooper()).post {
                            loadInitialData()
                        }
                    }

                    "exit_kinestex" -> {
                        logger.info("Exit KinesteX - clearing WebView history")
                        // Clear WebView history and load blank page on main thread
                        Handler(Looper.getMainLooper()).post {
                            try {
                                webView?.clearHistory()
                                webView?.loadUrl("about:blank")
                                logger.success("WebView history cleared after exit")
                            } catch (e: Exception) {
                                logger.error("Failed to clear WebView history", e)
                            }
                        }
                    }
                }

                val webViewMessage = when (type) {
                    "kinestex_launched" -> WebViewMessage.KinestexLaunched(dataMap)
                    "kinestex_loaded" -> WebViewMessage.KinestexLoaded(dataMap)
                    "finished_workout" -> WebViewMessage.FinishedWorkout(dataMap)
                    "error_occurred" -> WebViewMessage.ErrorOccurred(dataMap)
                    "exercise_completed" -> WebViewMessage.ExerciseCompleted(dataMap)
                    "exit_kinestex" -> WebViewMessage.ExitKinestex(dataMap)
                    "workout_opened" -> WebViewMessage.WorkoutOpened(dataMap)
                    "workout_started" -> WebViewMessage.WorkoutStarted(dataMap)
                    "plan_unlocked" -> WebViewMessage.PlanUnlocked(dataMap)
                    "mistake" -> WebViewMessage.Mistake(dataMap)
                    "successful_repeat" -> WebViewMessage.Reps(dataMap)
                    "left_camera_frame" -> WebViewMessage.LeftCameraFrame(dataMap)
                    "returned_camera_frame" -> WebViewMessage.ReturnedCameraFrame(dataMap)
                    "workout_overview" -> WebViewMessage.WorkoutOverview(dataMap)
                    "exercise_overview" -> WebViewMessage.ExerciseOverview(dataMap)
                    "workout_completed" -> WebViewMessage.WorkoutCompleted(dataMap)
                    "all_resources_loaded" -> WebViewMessage.AllResourcesLoaded(dataMap)
                    "workout_exit_request" -> WebViewMessage.WorkoutExitRequest(dataMap)
                    else -> WebViewMessage.CustomType(dataMap)
                }


                // Invoke callback (can be called from any thread)
                onMessageReceived?.invoke(webViewMessage)

            } catch (e: Exception) {
                logger.error("Error in messageHandler: $e")
                e.printStackTrace()
            }
        }
    }
}