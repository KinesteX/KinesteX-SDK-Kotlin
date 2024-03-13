package com.kinestex.kinestexsdkkotlin


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kinestex.kinestexsdkkotlin.models.MessageCallback
import com.kinestex.kinestexsdkkotlin.models.PlanCategory
import com.kinestex.kinestexsdkkotlin.models.WorkoutCategory


class KinesteXWebView(
    context: Context,
    private var apiKey: String,
    private var companyName: String,
    private var userId: String,
    private var planCategory: PlanCategory,
    private var workoutCategory: WorkoutCategory,
    private val messageCallback: MessageCallback
) {
    private var planCatString: String? = null
    private var workoutCatString: String? = null

    var webView: WebView = WebView(context).apply {
        validateInput(
            planCategory = planCategory,
            workoutCategory = workoutCategory,
        )
        setupWebView(this)
    }

    private fun validateInput(
        planCategory: PlanCategory,
        workoutCategory: WorkoutCategory
    ): String? {

        when (planCategory) {
            is PlanCategory.Cardio -> this.planCatString = "Cardio"
            is PlanCategory.WeightManagement -> this.planCatString = "Weight Management"
            is PlanCategory.Strength -> this.planCatString = "Strength"
            is PlanCategory.Rehabilitation -> this.planCatString = "Rehabilitation"
            is PlanCategory.Custom -> {
                if (planCategory.description.isEmpty()) {
                    Log.e("KinesteX iError:", "planCategory cannot be empty")
                    return "planCategory cannot be empty"
                } else if (containsDisallowedCharacters(planCategory.description)) {
                    Log.e("KinesteX iError:", "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', \$, ., #, or <script>")
                    return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                }

                this.planCatString = planCategory.description
            }
        }

        when (workoutCategory) {
            is WorkoutCategory.Fitness -> this.workoutCatString = "Fitness"
            is WorkoutCategory.Rehabilitation -> this.workoutCatString = "Rehabilitation"
            is WorkoutCategory.Custom -> {
                if (workoutCategory.description.isEmpty()) {
                    Log.e("KinesteX iError:", "workoutCategory cannot be empty")

                    return "workoutCategory cannot be empty"

                } else if (containsDisallowedCharacters(workoutCategory.description)) {
                    Log.e("KinesteX iError:", "workoutCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', \$, ., #, or <script>")

                    return "workoutCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"

                }

                this.workoutCatString = workoutCategory.description
            }
        }

        return null
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView(webView: WebView) {
        with(webView) {
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    injectJavaScript(view)
                }
            }
            settings.javaScriptEnabled = true
            addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) {
                    messageCallback.onMessageReceived(message)
                }
            }, "messageHandler")
            loadUrl("https://kineste-x-w.vercel.app")
        }
    }

    private fun injectJavaScript(view: WebView?) {

        val script = """
            window.postMessage({
                'key': '$apiKey',
                'company': '$companyName',
                'userId': '$userId',
                'planC': '$planCatString',
                'category': '$workoutCatString'
            });
        """.trimIndent()
        view?.evaluateJavascript(script, null)
    }


    companion object {

        private fun checkInput(
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory,
            workoutCategory: WorkoutCategory
        ): String? {
            // Perform validation checks here
            // Return null if validation is successful, or an error message string if not
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                )
            ) {
                return "apiKey, companyName, or userId contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
            }

            when (planCategory) {

                is PlanCategory.Custom -> {
                    if (planCategory.description.isEmpty()) {
                        return "planCategory cannot be empty"
                    } else if (containsDisallowedCharacters(planCategory.description)) {
                        return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                    }
                }

                else -> {
                    return null
                }
            }

            when (workoutCategory) {

                is WorkoutCategory.Custom -> {
                    if (workoutCategory.description.isEmpty()) {
                        return "workoutCategory cannot be empty"
                    } else if (containsDisallowedCharacters(workoutCategory.description)) {
                        return "workoutCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                    }
                }

                else -> {
                    return null
                }
            }

            return null
        }


        private fun containsDisallowedCharacters(input: String): Boolean {
            val disallowedCharacters = setOf(
                '<',
                '>',
                '{',
                '}',
                '(',
                ')',
                '[',
                ']',
                ';',
                '"',
                '\'',
                '$',
                '.',
                '#',
                '<',
                '>'
            )
            return input.any { it in disallowedCharacters }
        }


        @JvmStatic
        fun createWebView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory,
            workoutCategory: WorkoutCategory,
            callback: MessageCallback
        ): KinesteXWebView? {

            val validationError = checkInput(apiKey, companyName, userId, planCategory, workoutCategory)

            if (validationError != null) {
                Log.e("KinestexWebViewLib", "createWebView: $validationError" )
                return null
            }

            return KinesteXWebView(
                context,
                apiKey,
                companyName,
                userId,
                planCategory,
                workoutCategory,
                callback
            )
        }
    }
}
