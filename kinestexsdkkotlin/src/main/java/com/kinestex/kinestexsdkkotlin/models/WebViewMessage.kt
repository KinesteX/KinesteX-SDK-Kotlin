package com.kinestex.kinestexsdkkotlin.models

/**
 * Sealed class representing messages received from the KinesteX WebView
 *
 * These messages are sent from the WebView to communicate workout events,
 * errors, and user interactions back to the host application.
 *
 * @property data Map containing message-specific data
 */
sealed class WebViewMessage {
    /**
     * Sent when KinesteX view has finished launching
     * @param data Event data
     */
    data class KinestexLaunched(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a workout has been completed */
    data class FinishedWorkout(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when an error occurs during workout execution */
    data class ErrorOccurred(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when an individual exercise has been completed */
    data class ExerciseCompleted(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when user clicks the exit button */
    data class ExitKinestex(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a workout is opened/selected */
    data class WorkoutOpened(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a workout is started */
    data class WorkoutStarted(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a plan is unlocked */
    data class PlanUnlocked(val data: Map<String, Any>) : WebViewMessage()

    /** Custom message type for application-specific events */
    data class CustomType(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a successful repetition is completed */
    data class Reps(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a mistake is detected in exercise form */
    data class Mistake(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when user leaves the camera frame */
    data class LeftCameraFrame(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when user returns to the camera frame */
    data class ReturnedCameraFrame(val data: Map<String, Any>) : WebViewMessage()

    /** Sent with workout overview and statistics */
    data class WorkoutOverview(val data: Map<String, Any>) : WebViewMessage()

    /** Sent with exercise overview and instructions */
    data class ExerciseOverview(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a complete workout session is finished */
    data class WorkoutCompleted(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a custom workout resources are loaded */
    data class AllResourcesLoaded(val data: Map<String, Any>) : WebViewMessage()

    /** Sent when a custom workout exit requested */
    data class WorkoutExitRequest(val data: Map<String, Any>) : WebViewMessage()

}