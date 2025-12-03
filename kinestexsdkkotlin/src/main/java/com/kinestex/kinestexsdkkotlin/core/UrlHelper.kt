package com.kinestex.kinestexsdkkotlin.core

import android.net.Uri
import android.util.Log
import com.kinestex.kinestexsdkkotlin.api.ContentType

object UrlHelper {
    private const val BASE_URL = "https://ai.kinestex.com"
    private const val ADMIN_BASE_URL = "https://admin.kinestex.com"

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

    fun customWorkout(): String = "$BASE_URL/custom-workout"

    fun experienceView(experience: String): String {
        val encoded = Uri.encode(experience.lowercase())
        return "$BASE_URL/experiences/$encoded"
    }

    fun personalizedPlanView(): String = "$BASE_URL/personalized-plan"

    fun adminView(
        contentType: ContentType?,
        contentId: String?,
        customQueries: Map<String, Any?>? = null
    ): String {
        val hasType = contentType != null
        val hasId = !contentId.isNullOrBlank()

        if (hasType != hasId) {
            Log.e("KinesteX", "contentType and contentId must be provided together")
            return ADMIN_BASE_URL
        }

        if (hasId && containsDisallowedCharacters(contentId!!)) {
            Log.e("KinesteX", "contentId contains disallowed characters")
            return ADMIN_BASE_URL
        }

        val pathSegments = if (hasType && hasId) {
            listOf(segmentFor(contentType), contentId)
        } else {
            listOf("main")
        }

        val uri = Uri.Builder()
            .scheme("https")
            .authority("admin.kinestex.com")
            .apply { pathSegments.forEach { appendPath(it) } }
            .appendQueryParameter("isCustomAuth", "true")
            .appendQueryParameter("hideSidebar", "true")
            .apply {
                customQueries?.forEach { (key, value) ->
                    if (value != null) appendQueryParameter(key, value.toString())
                }
            }
            .build()

        return uri.toString()
    }

    fun cameraView(): String = "$BASE_URL/camera"

    /**
     * Converts ContentType enum to URL segment string
     */
    private fun segmentFor(contentType: ContentType?): String {
        return when (contentType) {
            ContentType.WORKOUT -> "workouts"
            ContentType.PLAN -> "plans"
            ContentType.EXERCISE -> "exercises"
            null -> "main"
        }
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