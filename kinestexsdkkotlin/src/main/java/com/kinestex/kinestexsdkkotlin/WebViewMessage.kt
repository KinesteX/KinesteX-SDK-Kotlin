package com.kinestex.kinestexsdkkotlin

sealed class WebViewMessage {
    data class KinestexLaunched(val data: Map<String, Any>) : WebViewMessage()
    data class FinishedWorkout(val data: Map<String, Any>) : WebViewMessage()
    data class ErrorOccurred(val data: Map<String, Any>) : WebViewMessage()
    data class ExerciseCompleted(val data: Map<String, Any>) : WebViewMessage()
    data class ExitKinestex(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutOpened(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutStarted(val data: Map<String, Any>) : WebViewMessage()
    data class PlanUnlocked(val data: Map<String, Any>) : WebViewMessage()
    data class CustomType(val data: Map<String, Any>) : WebViewMessage()
    data class Reps(val data: Map<String, Any>) : WebViewMessage()
    data class Mistake(val data: Map<String, Any>) : WebViewMessage()
    data class LeftCameraFrame(val data: Map<String, Any>) : WebViewMessage()
    data class ReturnedCameraFrame(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutOverview(val data: Map<String, Any>) : WebViewMessage()
    data class ExerciseOverview(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutCompleted(val data: Map<String, Any>) : WebViewMessage()

}

