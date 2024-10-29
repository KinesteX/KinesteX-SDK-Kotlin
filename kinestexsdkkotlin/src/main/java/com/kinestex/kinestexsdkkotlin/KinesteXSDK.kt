package com.kinestex.kinestexsdkkotlin

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableStateFlow


class KinesteXSDK {
    companion object {

        private var cameraWebView: GenericWebView? = null

        private const val how_to_video_link =
            "https://cdn.kinestex.com/SDK%2Fhow-to-video%2Fhowtovideo.webm?alt=media&token=9c1254eb-0726-4eed-b16e-4e3945c98b65"

        private var videoPlayer: ExoPlayer? = null
        fun createHowToView(
            context: Context,
            onVideoEnd: (Boolean) -> Unit,
            videoURL: String? = how_to_video_link,  // Default value for videoURL
            onCloseClick: () -> Unit
        ): ViewGroup {
            val frameLayout = FrameLayout(context)
            frameLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            videoPlayer = ExoPlayer.Builder(context).build()
            val playerView = PlayerView(context)
            playerView.player = videoPlayer
            playerView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                playerView.findViewById<View>(androidx.media3.ui.R.id.exo_next)?.visibility =
                    View.GONE
                playerView.findViewById<View>(androidx.media3.ui.R.id.exo_prev)?.visibility =
                    View.GONE
            })

            frameLayout.addView(playerView)

            val closeButton = ImageButton(context).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        android.R.drawable.ic_menu_close_clear_cancel
                    )
                )
                background = null
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    setMargins(16, 16, 16, 16)
                }
                setOnClickListener {
                    videoPlayer?.stop()
                    videoPlayer?.release()
                    onCloseClick()
                }
            }
            frameLayout.addView(closeButton)

            // Use the passed videoURL or the default one if not provided
            val mediaItem = MediaItem.fromUri(videoURL ?: how_to_video_link)
            videoPlayer?.setMediaItem(mediaItem)
            videoPlayer?.prepare()
            videoPlayer?.play()

            videoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onVideoEnd(true)
                    }
                }
            })

            return frameLayout
        }

        private fun validateInput(
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory,
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
                    if (planCategory.name.isEmpty()) {
                        return "planCategory cannot be empty"
                    } else if (containsDisallowedCharacters(planCategory.name)) {
                        return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
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

        private fun planCategoryString(category: PlanCategory): String {
            return when (category) {
                PlanCategory.Cardio -> "Cardio"
                PlanCategory.WeightManagement -> "Weight Management"
                PlanCategory.Strength -> "Strength"
                PlanCategory.Rehabilitation -> "Rehabilitation"
                is PlanCategory.Custom -> category.name
            }
        }

        private fun genderString(gender: Gender): String {
            return when (gender) {
                Gender.MALE -> "Male"
                Gender.FEMALE -> "Female"
                Gender.UNKNOWN -> "Male"
            }
        }

        private fun lifestyleString(lifestyle: Lifestyle): String {
            return when (lifestyle) {
                Lifestyle.SEDENTARY -> "Sedentary"
                Lifestyle.SLIGHTLY_ACTIVE -> "Slightly Active"
                Lifestyle.ACTIVE -> "Active"
                Lifestyle.VERY_ACTIVE -> "Very Active"
            }
        }

        fun createMainView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory = PlanCategory.Cardio,
            user: UserDetails?,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            val validationError = validateInput(apiKey, companyName, userId, planCategory)

            if (validationError != null) {
                Log.e("WebViewManager", "⚠️ Validation Error: $validationError")
                return null
            } else {
                val data = mutableMapOf<String, Any>(
                    "planC" to planCategoryString(planCategory)
                )

                user?.let {
                    data["age"] = it.age
                    data["height"] = it.height
                    data["weight"] = it.weight
                    data["gender"] = genderString(it.gender)
                    data["lifestyle"] = lifestyleString(it.lifestyle)
                }

                validateCustomParams(customParams, data)

                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex.vercel.app",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    permissionHandler = permissionHandler
                )
            }
        }

        /**
        Creates a view for a specific workout plan. Keeps track of the progress for that particular plan, recommending the workouts according to the person's progression

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework provided by KinesteX
        - userId: The unique identifier for the user.
        - planName: The name of the workout plan.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createPlanView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planName: String,
            user: UserDetails?,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(planName)
            ) {
                Log.e(
                    "WebViewManager",
                    "⚠️ Validation Error: apiKey, companyName, userId, or planName contains disallowed characters"
                )
                return null
            } else {
                val adjustedPlanName = planName.replace(" ", "%20")
                val url =
                    "https://kinestex.vercel.app/plan/$adjustedPlanName"
                val data = mutableMapOf<String, Any>()

                user?.let {
                    data["age"] = it.age
                    data["height"] = it.height
                    data["weight"] = it.weight
                    data["gender"] = genderString(it.gender)
                    data["lifestyle"] = lifestyleString(it.lifestyle)
                }

                validateCustomParams(customParams, data)

                return GenericWebView(
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = url,
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    context = context,
                    permissionHandler = permissionHandler
                )
            }
        }

        /**
        Creates a view for a specific workout.

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework.
        - userId: The unique identifier for the user.
        - workoutName: The name of the workout.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createWorkoutView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            workoutName: String,
            user: UserDetails?,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(workoutName)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or workoutName contains disallowed characters"
                )
                return null
            } else {
                val adjustedWorkoutName = workoutName.replace(" ", "%20")
                val url =
                    "https://kinestex.vercel.app/workout/$adjustedWorkoutName"

                val data: MutableMap<String, Any> = mutableMapOf(
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                validateCustomParams(customParams, data)


                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = url,
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    permissionHandler = permissionHandler
                )
            }
        }

        /**
        Creates a view for a specific exercise challenge.

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework.
        - userId: The unique identifier for the user.
        - exercise: The name of the exercise (default is "Squats").
        - countdown: The countdown time for the challenge.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createChallengeView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            exercise: String,
            countdown: Int,
            user: UserDetails?,
            customParams: MutableMap<String, Any>?,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(exercise)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or exercise contains disallowed characters"
                )
                return null
            } else {
                val data: MutableMap<String, Any> = mutableMapOf(
                    "exercise" to exercise,
                    "countdown" to countdown,
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                validateCustomParams(customParams, data)


                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex.vercel.app/challenge",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    permissionHandler = permissionHandler
                )
            }
        }

        fun createExperiencesView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            experienceName: String,
            countdown: Int,
            user: UserDetails?,
            customParams: MutableMap<String, Any>?,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(experienceName)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or exercise contains disallowed characters"
                )
                return null
            } else {
                val data: MutableMap<String, Any> = mutableMapOf(
                    "countdown" to countdown,
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                validateCustomParams(customParams, data)


                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex.vercel.app/experiences/${experienceName.lowercase()}",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    permissionHandler = permissionHandler
                )
            }
        }

        fun createCameraComponent(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            exercises: List<String>,
            currentExercise: String,
            user: UserDetails?,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            for (exercise in exercises) {
                if (containsDisallowedCharacters(exercise)) {
                    Log.e(
                        "WebViewManager",
                        "Validation Error: $exercise contains disallowed characters"
                    )
                    return null
                }
            }
            if (containsDisallowedCharacters(apiKey)
                || containsDisallowedCharacters(companyName)
                || containsDisallowedCharacters(
                    userId
                )
                || containsDisallowedCharacters(currentExercise)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or currentExercise contains disallowed characters"
                )
                return null
            } else {
                val data: MutableMap<String, Any> = mutableMapOf(
                    "exercises" to exercises,
                    "currentExercise" to currentExercise,
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                validateCustomParams(customParams, data)

                val cameraWebViewInstance = GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex-camera-ai.vercel.app",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    permissionHandler = permissionHandler
                )

                cameraWebView = cameraWebViewInstance
                return cameraWebViewInstance
            }
        }

        private fun validateCustomParams(
            customParams: MutableMap<String, Any>?,
            data: MutableMap<String, Any>
        ) {
            customParams?.let {
                for ((key, value) in customParams) {
                    if (containsDisallowedCharacters(key) || (value as? String)?.let {
                            containsDisallowedCharacters(
                                it
                            )
                        } == true) {
                        println("⚠️ Validation Error: Custom parameter key or value contains disallowed characters")
                    } else {
                        data[key] = value
                    }
                }
            }
        }

        /**
         * Updates the current exercise in the camera component.
         *
         * @param exercise The name of the current exercise.
         */
        fun updateCurrentExercise(exercise: String) {
            cameraWebView?.updateCurrentExercise(exercise)
        }

    }
}
