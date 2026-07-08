package com.kinestex.kinestexsdkkotlin.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.R
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    private val overlayView: FrameLayout = FrameLayout(context).apply {
        setBackgroundColor(overlayColor)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    private val foregroundColor: Int =
        if (isColorDark(overlayColor)) Color.WHITE else Color.BLACK

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var launched = false
    @Volatile
    private var launchTimer: Runnable? = null
    private var retryMessage = ""
    private var retryMessageView: TextView? = null

    private val connectionMessage: String
        get() = connectionMessageFor(data["language"])

    private val retryView: View by lazy { buildRetryView() }

    init {
        setupView()
    }

    private fun setupView() {
        setBackgroundColor(Color.BLACK)
        loadView()
        controller.getWebView()?.let { webView ->
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            addView(
                webView, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
        }
        overlayView.addView(buildBackButton())
        addView(overlayView)
        addView(retryView)
    }

    private fun buildBackButton(): ImageView = ImageView(context).apply {
        setImageResource(R.drawable.ic_arrow_left)
        setColorFilter(foregroundColor)
        scaleType = ImageView.ScaleType.FIT_CENTER
        val pad = dp(10)
        setPadding(pad, pad, pad, pad)
        layoutParams = FrameLayout.LayoutParams(dp(40), dp(40)).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = dp(16)
            topMargin = dp(16)
        }
        setOnClickListener { exit() }
    }

    // Load the view using the singleton controller.

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadView() {
        launched = false
        retryView.visibility = View.GONE
        overlayView.visibility = View.VISIBLE
        startLaunchTimer()

        controller.loadView(
            context = context,
            apiKey = apiKey,
            companyName = companyName,
            userId = userId,
            url = url,
            data = data,
            permissionHandler = permissionHandler,
            onMessageReceived = { message ->
                when {
                    message is WebViewMessage.KinestexLaunched -> {
                        launched = true
                        cancelLaunchTimer()
                        hideRetry()
                    }

                    message is WebViewMessage.KinestexLoaded -> {
                        cancelLaunchTimer()
                        hideRetry()
                        mainHandler.postDelayed({
                            overlayView.visibility = View.GONE
                        }, 200)
                    }

                    message is WebViewMessage.ErrorOccurred && !launched -> {
                        showRetryScreen(errorMessageFrom(message))
                    }
                }
                onMessageReceived(message)
            },
            isLoading = isLoading
        )
    }

    private fun startLaunchTimer() {
        cancelLaunchTimer()
        val timer = Runnable {
            logger.error("KinesteX did not launch within 7s — showing retry")
            showRetryScreen(connectionMessage)
        }
        launchTimer = timer
        mainHandler.postDelayed(timer, 7000)
    }

    private fun cancelLaunchTimer() {
        launchTimer?.let { mainHandler.removeCallbacks(it) }
        launchTimer = null
    }

    private fun showRetryScreen(message: String) {
        cancelLaunchTimer()
        mainHandler.post {
            retryMessage = message
            retryMessageView?.text = message
            retryView.bringToFront()
            retryView.visibility = View.VISIBLE
        }
    }

    private fun hideRetry() {
        mainHandler.post { retryView.visibility = View.GONE }
    }

    private fun errorMessageFrom(message: WebViewMessage.ErrorOccurred): String {
        val data = message.data
        val nested = data["data"]
        val text: String? = when {
            nested is String -> nested
            nested is Map<*, *> && nested["message"] is String -> nested["message"] as String
            data["message"] is String -> data["message"] as String
            else -> null
        }
        return if (text != null && text.trim().isNotEmpty()) text.trim() else connectionMessage
    }

    private fun buildRetryView(): View {
        val container = FrameLayout(context).apply {
            setBackgroundColor(overlayColor)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        val backButton = buildBackButton()

        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val hPad = dp(24)
            setPadding(hPad, 0, hPad, 0)
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
            isClickable = true
            setOnClickListener { loadView() }
        }

        val refreshIcon = ImageView(context).apply {
            setImageResource(R.drawable.ic_refresh)
            setColorFilter(foregroundColor)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
        }

        val messageView = TextView(context).apply {
            text = retryMessage
            setTextColor(foregroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        retryMessageView = messageView

        column.addView(refreshIcon)
        column.addView(messageView)

        container.addView(column)
        container.addView(backButton)
        return container
    }

    private fun exit() {
        val exitData = mapOf<String, Any>(
            "type" to "exit_kinestex",
            "timestamp" to isoTimestamp()
        )
        onMessageReceived(WebViewMessage.ExitKinestex(exitData))
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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
            cancelLaunchTimer()
            mainHandler.removeCallbacksAndMessages(null)
            removeAllViews()
            logger.success("GenericWebView wrapper cleaned up")
        } catch (e: Exception) {
            logger.error("Error cleaning up GenericWebView wrapper", e)
        }
    }
}

private fun isColorDark(color: Int): Boolean {
    val luminance = (0.299 * Color.red(color) +
            0.587 * Color.green(color) +
            0.114 * Color.blue(color)) / 255
    return luminance < 0.5
}

private fun isoTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date())

private fun connectionMessageFor(language: Any?): String {
    var code = (language as? String)?.lowercase()?.trim() ?: ""
    code = code.split(Regex("[-_]")).first()
    code = mapOf("iw" to "he", "in" to "id")[code] ?: code
    return connectionMessages[code] ?: connectionMessages["en"]!!
}

private val connectionMessages = mapOf(
    "en" to "Please connect to the internet and try again",
    "ar" to "يرجى الاتصال بالإنترنت والمحاولة مرة أخرى",
    "es" to "Conéctate a Internet e inténtalo de nuevo",
    "it" to "Connettiti a Internet e riprova",
    "he" to "אנא התחבר לאינטרנט ונסה שוב",
    "zh" to "请连接到互联网后重试",
    "pt" to "Conecte-se à Internet e tente novamente",
    "uk" to "Підключіться до Інтернету та повторіть спробу",
    "bn" to "অনুগ্রহ করে ইন্টারনেটে সংযুক্ত হয়ে আবার চেষ্টা করুন",
    "ru" to "Пожалуйста, подключитесь к интернету и повторите попытку",
    "hi" to "कृपया इंटरनेट से कनेक्ट करें और पुनः प्रयास करें",
    "id" to "Silakan sambungkan ke internet dan coba lagi",
    "fr" to "Veuillez vous connecter à Internet et réessayer",
    "el" to "Συνδεθείτε στο διαδίκτυο και δοκιμάστε ξανά",
    "nl" to "Maak verbinding met internet en probeer het opnieuw",
    "de" to "Bitte stellen Sie eine Internetverbindung her und versuchen Sie es erneut",
    "uz" to "Iltimos, internetga ulaning va qayta urinib ko‘ring",
    "kk" to "Интернетке қосылып, қайталап көріңіз",
    "ja" to "インターネットに接続して、もう一度お試しください",
    "tr" to "Lütfen internete bağlanın ve tekrar deneyin",
    "ko" to "인터넷에 연결한 후 다시 시도해 주세요",
)

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

