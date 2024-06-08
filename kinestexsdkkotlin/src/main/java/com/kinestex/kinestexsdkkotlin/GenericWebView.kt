package com.kinestex.kinestexsdkkotlin


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@SuppressLint("ViewConstructor")
class GenericWebView(
    context: Context,
    apiKey: String,
    companyName: String,
    userId: String,
    url: String,
    onMessageReceived: (WebViewMessage) -> Unit,
    isLoading: MutableStateFlow<Boolean>,
    data: Map<String, Any>
) : WebView(context) {
    private var viewModel: WebViewState = WebViewState()

    init {
        createGenericWebView(
            this,
            apiKey,
            companyName,
            userId,
            url,
            isLoading,
            data,
            onMessageReceived
        )
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun createGenericWebView(
        webView: WebView,
        apiKey: String,
        companyName: String,
        userId: String,
        url: String,
        isLoading: MutableStateFlow<Boolean>,
        data: Map<String, Any>,
        messageCallback: (WebViewMessage) -> Unit
    ) {
        viewModel.webView.value = webView

        with(webView) {
            webChromeClient = WebChromeClient()

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        request.grant(request.resources)
                    } else {
                        request.deny()
                    }
                }
            }


            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading.value = false

                    postMessage(
                        view,
                        apiKey,
                        companyName,
                        userId,
                        data,
                        url
                    )
                }
            }

            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false // Ensure media playback is allowed
            addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) {
                    handleMessage(message, messageCallback)
                }
            }, "messageHandler")

            loadUrl(url)
        }
    }

    private fun handleMessage(message: String, messageCallback: (WebViewMessage) -> Unit) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type")

            val dataMap = mutableMapOf<String, Any>()
            json.keys().forEach { key ->
                dataMap[key] = json[key]
            }

            val webViewMessage = when (type) {
                "kinestex_launched" -> WebViewMessage.KinestexLaunched(dataMap)
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
                else -> WebViewMessage.CustomType(dataMap)
            }

            // Invoke the callback with the parsed message
            messageCallback(webViewMessage)
        } catch (e: JSONException) {
            Log.e("WebView", "Error parsing JSON message: $message", e)
        }
    }

    fun postMessage(
        webView: WebView?,
        apiKey: String?,
        companyName: String,
        userId: String,
        data: Map<String, Any?>,
        url: String?
    ) {
        val script = buildString {
            append("window.postMessage({")
            append("'key': '${apiKey}', ")
            append("'company': '${companyName}', ")
            append("'userId': '${userId}', ")
            append("'exercises': ${jsonString(data["exercises"] as? List<String> ?: emptyList())}, ")
            append("'currentExercise': '${data["currentExercise"] as? String ?: ""}'")

            data.forEach { (key, value) ->
                if (key != "exercises" && key != "currentExercise") {
                    append(", '$key': '$value'")
                }
            }
            append("}, '${url}');")
        }

        webView?.evaluateJavascript(script) { result ->
            if (result != null) {
                Log.d("WebView", "Result: $result")
            }
        }
    }

    private fun jsonString(from: List<String>): String {
        return try {
            val jsonArray = JSONArray(from)
            jsonArray.toString()
        } catch (e: JSONException) {
            "[]"
        }
    }

    fun updateCurrentExercise(exercise: String) {
        val webView = viewModel.webView.value

        val script = """
        window.postMessage({ 'currentExercise': '$exercise' }, '*');
    """.trimIndent()

        webView?.evaluateJavascript(script) { result ->
            Log.d("WebViewManager", "✅ Successfully sent an update: $result")
        }
    }
}
