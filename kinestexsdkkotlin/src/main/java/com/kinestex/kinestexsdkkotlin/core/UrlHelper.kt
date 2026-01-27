package com.kinestex.kinestexsdkkotlin.core

import android.net.Uri
import android.util.Log
import com.kinestex.kinestexsdkkotlin.api.ContentType
import com.kinestex.kinestexsdkkotlin.models.IStyle
import androidx.core.net.toUri

object UrlHelper {
    private const val BASE_URL = "https://ai.kinestex.com"
    private const val ADMIN_BASE_URL = "https://admin.kinestex.com"

    /**
     * Appends style query parameters to a base URL
     * Uses Uri.Builder to properly encode values (handles # in hex colors, etc.)
     */
    private fun appendStyleParams(baseUrl: String, style: IStyle): String {
        val params = style.toQueryParams()
        if (params.isEmpty()) return baseUrl

        val uri = baseUrl.toUri().buildUpon()
        params.forEach { (key, value) ->
            uri.appendQueryParameter(key, value)
        }
        return uri.build().toString()
    }

    fun mainView(style: IStyle): String = appendStyleParams(BASE_URL, style)

    fun planView(planName: String, style: IStyle): String {
        val encoded = Uri.encode(planName)
        return appendStyleParams("$BASE_URL/plan/$encoded", style)
    }

    fun workoutView(workoutName: String, style: IStyle): String {
        val encoded = Uri.encode(workoutName)
        return appendStyleParams("$BASE_URL/workout/$encoded", style)
    }

    fun challengeView(style: IStyle): String = appendStyleParams("$BASE_URL/challenge", style)

    fun leaderboardView(username: String = "", style: IStyle): String {
        val baseUrl = if (username.isNotEmpty()) {
            "$BASE_URL/leaderboard?username=$username"
        } else {
            "$BASE_URL/leaderboard"
        }
        return appendStyleParams(baseUrl, style)
    }

    fun customWorkout(style: IStyle): String = appendStyleParams("$BASE_URL/custom-workout", style)

    fun experienceView(experience: String, style: IStyle): String {
        val encoded = Uri.encode(experience.lowercase())
        return appendStyleParams("$BASE_URL/experiences/$encoded", style)
    }

    fun personalizedPlanView(style: IStyle): String = appendStyleParams("$BASE_URL/personalized-plan", style)

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

    fun cameraView(style: IStyle): String = appendStyleParams("$BASE_URL/camera", style)

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