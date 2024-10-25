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
    // Store the permission request to handle it after permission result
    private var pendingCameraPermissionRequest: PermissionRequest? = null


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.domStorageEnabled = true


        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d("KinesteX SDK", "Permission request received: ${request.resources.contentToString()}")

                try {
                    if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        // Store the request for later use
                        pendingCameraPermissionRequest = request

                        // Check current permission status
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                // If already granted, grant to WebView immediately
                                Log.d("KinesteX SDK", "Camera permission already granted, granting to WebView")
                                request.grant(request.resources)
                            }
                            (context as? Activity)?.shouldShowRequestPermissionRationale(
                                Manifest.permission.CAMERA
                            ) == true -> {
                                // Optional: Show rationale if needed
                                Log.d("KinesteX SDK", "Should show permission rationale")
                                requestCameraPermission()
                            }
                            else -> {
                                // Request permission
                                Log.d("KinesteX SDK", "Requesting camera permission")
                                requestCameraPermission()
                            }
                        }
                    } else {
                        Log.d("KinesteX SDK", "Denying non-camera permission request")
                        request.deny()
                    }
                } catch (e: Exception) {
                    Log.e("KinesteX SDK", "Error handling permission request", e)
                    request.deny()
                }
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading.value = false
                Log.d("KinesteX SDK", "Page finished loading")
                postMessage()
            }
        }

        addJavascriptInterface(JavaScriptInterface(), "messageHandler")
        loadUrl(url)
    }

    private fun requestCameraPermission() {
        val activity = context as? Activity
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            Log.e("KinesteX SDK", "Context is not an activity, cannot request permission")
            pendingCameraPermissionRequest?.deny()
            pendingCameraPermissionRequest = null
        }
    }

    // Method to be called from Activity's onRequestPermissionsResult
    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        Log.d("KinesteX SDK", "Handling permission result: code=$requestCode, results=${grantResults.contentToString()}")

        if (requestCode == CAMERA_PERMISSION_CODE) {
            pendingCameraPermissionRequest?.let { request ->
                try {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("KinesteX SDK", "Permission granted, granting to WebView")
                        request.grant(request.resources)
                    } else {
                        Log.d("KinesteX SDK", "Permission denied, denying WebView request")
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
    }

    companion object {
        const val CAMERA_PERMISSION_CODE = 1010101
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

