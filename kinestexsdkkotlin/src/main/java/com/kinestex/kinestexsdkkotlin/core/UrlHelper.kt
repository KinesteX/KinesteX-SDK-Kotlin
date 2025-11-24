package com.kinestex.kinestexsdkkotlin.core

import android.net.Uri

object UrlHelper {
    private const val BASE_URL = "https://kinestex.vercel.app"

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

    fun leaderboardView(username: String): String {
        val encoded = Uri.encode(username)
        return "$BASE_URL/leaderboard/$encoded?username=$username"
    }

    fun experienceView(experience: String): String {
        val encoded = Uri.encode(experience)
        return "$BASE_URL/experiences/$encoded"
    }

    fun personalizedPlanView(): String = "$BASE_URL/personalized-plan"

    fun cameraView(): String = "$BASE_URL/camera"
}