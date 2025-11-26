package com.kinestex.kinestexsdkkotlin.core

import android.net.Uri

object UrlHelper {
    private const val BASE_URL = "https://ai.kinestex.com"

    fun mainView(): String = BASE_URL

    fun planView(planName: String): String {
        val encoded = Uri.encode(planName)
        return "$BASE_URL/plan/$encoded"
    }

    fun workoutView(workoutName: String): String {
        val encoded = Uri.encode(workoutName)
        return "$BASE_URL/workout/$encoded"
    }

    fun challengeView(): String = "$BASE_URL/challenge"

    fun leaderboardView(username: String = ""): String {
        return if (username.isNotEmpty()) {
            "$BASE_URL/leaderboard?username=$username"
        } else {
            "$BASE_URL/leaderboard"
        }
    }

    fun experienceView(experience: String): String {
        val encoded = Uri.encode(experience.lowercase())
        return "$BASE_URL/experiences/$encoded"
    }

    fun customWorkouts(): String = "$BASE_URL/custom-workout"

    fun personalizedPlanView(): String = "$BASE_URL/personalized-plan"

    fun cameraView(): String = "$BASE_URL/camera"
}