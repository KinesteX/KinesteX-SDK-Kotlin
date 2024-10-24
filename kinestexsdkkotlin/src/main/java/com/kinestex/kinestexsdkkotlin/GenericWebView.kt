package com.kinestex.kinestexsdkkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@SuppressLint("ViewConstructor")
class GenericWebView(
    context: Context,
    private val apiKey: String,
    private val companyName: String,
    private val userId: String,
    private val url: String,
    private val onMessageReceived: (WebViewMessage) -> Unit,
    private val isLoading: MutableStateFlow<Boolean>,
    private val data: Map<String, Any>
) : WebView(context) {

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.domStorageEnabled = true

        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                val requestedResources = request.resources

                if (requestedResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                ) {
                    // Check if app-level permissions are granted before granting to the site
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        request.grant(requestedResources)
                    } else {
                        // Request app-level permissions if not granted yet
                        ActivityCompat.requestPermissions(
                            (context as Activity),
                            arrayOf(Manifest.permission.CAMERA),
                            1010101
                        )
                        request.deny()  // Deny until permissions are granted
                    }
                } else {
                    // Deny any other permissions
                    request.deny()
                }
            }
        }


        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading.value = false
                Log.d("WebView", "Page finished loading")
                postMessage()
            }
        }

        addJavascriptInterface(JavaScriptInterface(), "messageHandler")
        loadUrl(url)
    }

    private fun postMessage() {
        val message = JSONObject().apply {
            put("key", apiKey)
            put("company", companyName)
            put("userId", userId)
            put("exercises", JSONArray(data["exercises"] as? List<String> ?: emptyList<String>()))
            put("currentExercise", data["currentExercise"] as? String ?: "")
            for ((key, value) in data) {
                if (key !in listOf("exercises", "currentExercise")) {
                    put(key, value)
                }
            }
        }

        val script = """
            (function() {
                var event = new MessageEvent('message', {
                    data: $message,
                    origin: '$url',
                    source: window
                });
                window.dispatchEvent(event);
            })();
        """.trimIndent()

        Log.d("WebView", "Executing script: $script")
        evaluateJavascript(script) { result ->
            Log.d("WebView", "Script execution result: $result")
        }
    }

    fun updateCurrentExercise(exercise: String) {
        val script = """
            (function() {
                var event = new MessageEvent('message', {
                    data: { currentExercise: '$exercise' },
                    origin: '$url',
                    source: window
                });
                window.dispatchEvent(event);
            })();
        """.trimIndent()

        evaluateJavascript(script) { result ->
            Log.d("WebView", "Updated current exercise: $result")
        }
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            try {
                val json = JSONObject(message)
                val type = json.optString("type")
                val dataMap = json.toMap()

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

                onMessageReceived(webViewMessage)
            } catch (e: JSONException) {
                Log.e("WebView", "Error parsing JSON message: $message", e)
            }
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

