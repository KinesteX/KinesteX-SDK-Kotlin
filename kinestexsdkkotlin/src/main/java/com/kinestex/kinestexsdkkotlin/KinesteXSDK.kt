package com.kinestex.kinestexsdkkotlin

import android.content.Context
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
import com.kinestex.kinestexsdkkotlin.api.ContentType
import com.kinestex.kinestexsdkkotlin.api.KinesteXAPI
import com.kinestex.kinestexsdkkotlin.core.GenericWebView
import com.kinestex.kinestexsdkkotlin.core.KinesteXCredentials
import com.kinestex.kinestexsdkkotlin.core.KinesteXInitializer
import com.kinestex.kinestexsdkkotlin.core.KinesteXLogger
import com.kinestex.kinestexsdkkotlin.core.KinesteXViewBuilder
import com.kinestex.kinestexsdkkotlin.core.KinesteXWebViewController
import com.kinestex.kinestexsdkkotlin.core.UrlHelper
import com.kinestex.kinestexsdkkotlin.models.IStyle
import com.kinestex.kinestexsdkkotlin.models.PlanCategory
import com.kinestex.kinestexsdkkotlin.models.UserDetails
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import com.kinestex.kinestexsdkkotlin.models.WorkoutSequenceExercise
import kotlinx.coroutines.flow.MutableStateFlow


// ============================================================
// NEW ARCHITECTURE (v2.0) - Initialize-first pattern
// ============================================================

class KinesteXSDK {
    companion object {
        private val initializer = KinesteXInitializer()
        private val credentials = KinesteXCredentials()
        private var apiService: KinesteXAPI? = null
        private val logger = KinesteXLogger.instance
        private const val HOW_TO_VIDEO_LINK =
            "https://cdn.kinestex.com/SDK%2Fhow-to-video%2Fhowtovideo.webm?alt=media&token=9c1254eb-0726-4eed-b16e-4e3945c98b65"
        private var videoPlayer: ExoPlayer? = null

        /**
         * Initialize the KinesteX SDK
         *
         * Must be called before using any SDK features, typically in Application.onCreate()
         * This enables the new credential-free API pattern.
         *
         * @param context Application context
         * @param apiKey Your KinesteX API key
         * @param companyName Your company identifier
         * @param userId Current user identifier
         * @throws IllegalStateException if already initialized
         */
        fun initialize(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String
        ) {
            logger.info("Initializing KinesteX SDK...")

            // Initialize using the new architecture
            initializer.initialize(context, apiKey, companyName, userId)
            credentials.set(apiKey, companyName, userId)

            // Create API service instance
            apiService = KinesteXAPI(
                apiKey = apiKey,
                companyName = companyName
            )

            logger.success("KinesteX SDK initialized successfully")
        }

        /**
         * Check if SDK is initialized
         *
         * @return true if SDK has been initialized, false otherwise
         */
        fun isInitialized(): Boolean = initializer.getIsInitialized()

        /**
         * Get API service instance for fetching workout/plan/exercise data
         *
         * Example usage:
         * ```kotlin
         * val result = KinesteXSDK.api.fetchAPIContentData(
         *     contentType = ContentType.WORKOUT,
         *     category = "Strength"
         * )
         * ```
         *
         * @throws IllegalStateException if SDK not initialized
         */
        val api: KinesteXAPI
            get() = apiService
                ?: throw IllegalStateException(
                    "SDK not initialized. Call KinesteXSDK.initialize() first."
                )

        /**
         * Dispose SDK resources
         *
         * Cleans up WebView cache, releases resources, and resets initialization state.
         * SDK can be reinitialized after disposal if needed.
         */
        fun dispose() {
            logger.info("Disposing KinesteX SDK...")

            initializer.dispose()
            apiService = null

            // Also cleanup old static resources for safety
            cleanup()

            logger.success("KinesteX SDK disposed")
        }

        /**
         * Cleans up static resources to prevent memory leaks
         * Should be called when SDK resources are no longer needed
         *
         * NOTE: This is a temporary solution. In v2.0, static references will be removed entirely.
         */
        fun cleanup() {
            try {
                logger.info("Cleaning up static SDK resources...")

                // Clean up video player
                videoPlayer?.let { player ->
                    player.stop()
                    player.release()
                    videoPlayer = null
                }

                logger.success("Static SDK resources cleaned up")
            } catch (e: Exception) {
                logger.error("Error cleaning up static SDK resources", e)
            }
        }

        // ============================================================
        // NEW VIEW CREATION METHODS (v2.0) - Credential-free pattern
        // ============================================================

        /**
         * Creates a main view with category selection
         *
         * Requires SDK to be initialized first via `initialize()`
         *
         * @param context Activity or Fragment context
         * @param planCategory Workout plan category (default: Cardio)
         * @param user Optional user details (age, height, weight, etc.)
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for camera/storage permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createMainView(
            context: Context,
            planCategory: PlanCategory = PlanCategory.Cardio,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Build view-specific data
            val data = mapOf(
                "planC" to KinesteXViewBuilder.planCategoryString(planCategory)
            )

            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.mainView(style ?: IStyle()),
                style = style,
                data = data,
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a personalized plan view
         *
         * @param context Activity or Fragment context
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createPersonalizedPlanView(
            context: Context,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Delegate to centralized builder (no view-specific data)
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.personalizedPlanView(style ?: IStyle()),
                style = style,
                data = emptyMap(),
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a custom workout with given exercises
         *
         * @param context Activity or Fragment context
         * @param customWorkouts List of the exercises in custom workout
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createCustomWorkoutView(
            context: Context,
            customWorkouts: List<WorkoutSequenceExercise>,
            user: UserDetails? = null,
            isLoading: MutableStateFlow<Boolean>,
            style: IStyle? = null,
            customParams: Map<String, Any>? = null,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler,
        ): WebView? {

            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            val normalized = normalizeWorkoutExercises(customWorkouts)
            if (normalized == null) {
                logger.error("Validation Error: No valid exercises provided for custom workout")
                return null
            }

            return KinesteXViewBuilder.build(
                    context = context,
                    apiKey = credentials.apiKey,
                    companyName = credentials.companyName,
                    userId = credentials.userId,
                    url = UrlHelper.customWorkout(style ?: IStyle()),
                    style = style,
                    data = mapOf(
                        "customWorkoutExercises" to normalized
                    ),
                    user = user,
                    customParams = customParams,
                    isLoading = isLoading,
                    permissionHandler = permissionHandler,
                    onMessageReceived = onMessageReceived
            ) as WebView?

        }


        /**
         * Creates a plan view for a specific workout plan
         *
         * @param context Activity or Fragment context
         * @param planName Name of the workout plan
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createPlanView(
            context: Context,
            planName: String,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Delegate to centralized builder (no view-specific data)
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.planView(planName, style ?: IStyle()),
                style = style,
                data = emptyMap(),
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a workout view for a specific workout
         *
         * @param context Activity or Fragment context
         * @param workoutName Name of the workout
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createWorkoutView(
            context: Context,
            workoutName: String,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Delegate to centralized builder (no view-specific data)
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.workoutView(workoutName, style ?: IStyle()),
                style = style,
                data = emptyMap(),
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a challenge view for exercise competitions
         *
         * @param context Activity or Fragment context
         * @param exercise Exercise name
         * @param countdown Challenge countdown in seconds
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @param showLeaderboard Whether to show leaderboard
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createChallengeView(
            context: Context,
            exercise: String,
            countdown: Int,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler,
            showLeaderboard: Boolean = true
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Build view-specific data
            val data = mapOf(
                "exercise" to exercise,
                "countdown" to countdown,
                "showLeaderboard" to showLeaderboard
            )

            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.challengeView(style ?: IStyle()),
                style = style,
                data = data,
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a leaderboard view
         *
         * @param context Activity or Fragment context
         * @param exercise Exercise name for leaderboard
         * @param username Optional username filter
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createLeaderboardView(
            context: Context,
            exercise: String,
            username: String = "",
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Build view-specific data
            val data = mapOf(
                "exercise" to exercise
            )

            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.leaderboardView(username, style ?: IStyle()),
                style = style,
                data = data,
                user = null,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates an experiences view
         *
         * @param context Activity or Fragment context
         * @param experienceName Name of the experience
         * @param countdown Experience countdown
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createExperiencesView(
            context: Context,
            experienceName: String,
            countdown: Int,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Build view-specific data
            val data = mapOf(
                "countdown" to countdown
            )


            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.experienceView(experienceName, style ?: IStyle()),
                style = style,
                data = data,
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a camera component for custom exercise tracking
         *
         * @param context Activity or Fragment context
         * @param exercises List of exercise names
         * @param currentExercise Currently selected exercise
         * @param user Optional user details
         * @param customParams Optional custom parameters
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createCameraComponent(
            context: Context,
            exercises: List<String>,
            currentExercise: String,
            user: UserDetails? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            val credentials = credentials.get()

            // Build view-specific data
            val data = mapOf(
                "exercises" to exercises,
                "currentExercise" to currentExercise
            )

            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = UrlHelper.cameraView(style ?: IStyle()),
                style = style,
                data = data,
                user = user,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates an admin workout editor view
         *
         * Provides an admin interface for creating and editing workout content.
         * Requires admin credentials and permissions.
         *
         * @param context Activity or Fragment context
         * @param organization Organization identifier
         * @param contentType Optional content type to edit (WORKOUT, PLAN, or EXERCISE)
         * @param contentId Optional content ID to edit specific content
         * @param customParams Optional custom parameters
         * @param customQueries Optional custom query parameters for the URL
         * @param isLoading Loading state flow
         * @param onMessageReceived Callback for WebView messages
         * @param permissionHandler Handler for permissions
         * @return WebView instance or null on error
         * @throws IllegalStateException if SDK not initialized
         */
        fun createAdminWorkoutEditor(
            context: Context,
            organization: String,
            contentType: ContentType? = null,
            contentId: String? = null,
            style: IStyle? = null,
            customParams: MutableMap<String, Any>? = null,
            customQueries: Map<String, Any?>? = null,
            isLoading: MutableStateFlow<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit,
            permissionHandler: PermissionHandler
        ): WebView? {
            if (!isInitialized()) {
                throw IllegalStateException("SDK not initialized. Call KinesteXSDK.initialize() first.")
            }

            // Validate organization
            if (containsDisallowedCharacters(organization)) {
                logger.error("Validation Error: organization contains disallowed characters")
                return null
            }

            val credentials = credentials.get()

            // Build the admin view URL
            val url = UrlHelper.adminView(
                contentType = contentType,
                contentId = contentId,
                customQueries = customQueries
            )

            // Build view-specific data
            val data = mapOf(
                "organization" to organization,
                "apiKey" to credentials.apiKey,
                "companyName" to credentials.companyName
            )

            // Delegate to centralized builder
            return KinesteXViewBuilder.build(
                context = context,
                apiKey = credentials.apiKey,
                companyName = credentials.companyName,
                userId = credentials.userId,
                url = url,
                style = style,
                data = data,
                user = null,
                customParams = customParams,
                isLoading = isLoading,
                permissionHandler = permissionHandler,
                onMessageReceived = onMessageReceived
            ) as? WebView
        }

        /**
         * Creates a how-to video view (does not require initialization)
         *
         * Shows an instructional video with custom controls.
         * This is a standalone utility and doesn't require SDK initialization.
         *
         * @param context Activity or Fragment context
         * @param onVideoEnd Callback when video finishes
         * @param videoURL Optional custom video URL (defaults to KinesteX how-to video)
         * @param onCloseClick Callback when close button clicked
         * @return ViewGroup containing the video player
         */
        fun createHowToView(
            context: Context,
            onVideoEnd: (Boolean) -> Unit,
            videoURL: String? = HOW_TO_VIDEO_LINK,  // Default value for videoURL
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
            val mediaItem = MediaItem.fromUri(videoURL ?: HOW_TO_VIDEO_LINK)
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


        /**
         * Updates the current exercise in the camera component.
         *
         * Uses the singleton WebView controller to update the exercise dynamically.
         * This method works because all views share the same WebView instance.
         *
         * @param exercise The name of the current exercise.
         */
        fun updateCurrentExercise(exercise: String) {
            KinesteXWebViewController.getInstance().updateCurrentExercise(exercise)
        }

        fun normalizeWorkoutExercises(
            exercises: List<WorkoutSequenceExercise>?
        ): List<Map<String, Any?>>? {

            if (exercises == null || exercises.isEmpty()) {
                return null
            }

            val normalizedExercises = mutableListOf<Map<String, Any?>>()

            for (exercise in exercises) {

                // Validate exerciseId
                if (exercise.exerciseId.isEmpty() ||
                    containsDisallowedCharacters(exercise.exerciseId)
                ) {
                    continue
                }

                // Validate numeric values
                if ((exercise.reps != null && exercise.reps < 0) ||
                    (exercise.duration != null && exercise.duration < 0) ||
                    exercise.restDuration < 0
                ) {
                    continue
                }

                normalizedExercises.add(
                    mapOf(
                        "exerciseId" to exercise.exerciseId,
                        "reps" to exercise.reps,
                        "duration" to exercise.duration,
                        "includeRestPeriod" to exercise.includeRestPeriod,
                        "restDuration" to exercise.restDuration
                    )
                )
            }

            return if (normalizedExercises.isEmpty()) null else normalizedExercises
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
                '>',
                '`'
            )
            return input.any { it in disallowedCharacters }
        }
    }
}
