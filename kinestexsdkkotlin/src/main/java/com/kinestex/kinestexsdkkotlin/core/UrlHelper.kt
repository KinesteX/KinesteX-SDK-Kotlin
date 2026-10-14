package com.kinestex.kinestexsdkkotlin.core

import android.net.Uri
import android.util.Log
import com.kinestex.kinestexsdkkotlin.api.ContentType
import com.kinestex.kinestexsdkkotlin.models.IStyle
import androidx.core.net.toUri
import java.net.URI
import java.net.URISyntaxException

object UrlHelper {
    internal const val DEFAULT_BASE_URL = "https://ai.kinestex.com"
    internal const val DEFAULT_ADMIN_BASE_URL = "https://admin.kinestex.com"

    /**
     * App host for every view and the warmup page. Set by [com.kinestex.kinestexsdkkotlin.KinesteXSDK.initialize];
     * regional deployments (e.g. KSA) serve the app from their own host.
     */
    @Volatile
    internal var appBaseUrl: String = DEFAULT_BASE_URL

    /** Admin dashboard origin for [adminView]; the same origin serves the Content API. */
    @Volatile
    internal var adminBaseUrl: String = DEFAULT_ADMIN_BASE_URL

    internal fun warmupUrl(): String = "$appBaseUrl/warmup"

    /**
     * Validates a host override passed to KinesteXSDK.initialize and returns it without a trailing "/".
     * Throws instead of falling back: a silent fallback would send a regional tenant's traffic and data
     * to another region.
     */
    internal fun requireHttpsUrl(value: String, name: String): String {
        val url = value.trim().trimEnd('/')
        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            null
        }
        require(
            uri != null &&
                uri.scheme.equals("https", ignoreCase = true) &&
                !uri.host.isNullOrEmpty() &&
                uri.rawQuery == null &&
                uri.rawFragment == null
        ) {
            "KinesteXSDK.initialize: $name must be an https URL with a host, got \"$value\""
        }
        return url
    }

    /** Scheme and host (with port, if any) of an already validated URL. */
    internal fun originOf(url: String): String = URI(url).let { "${it.scheme}://${it.rawAuthority}" }

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

    fun mainView(style: IStyle): String = appendStyleParams(appBaseUrl, style)

    fun planView(planName: String, style: IStyle): String {
        val encoded = Uri.encode(planName)
        return appendStyleParams("$appBaseUrl/plan/$encoded", style)
    }

    fun workoutView(workoutName: String, style: IStyle): String {
        val encoded = Uri.encode(workoutName)
        return appendStyleParams("$appBaseUrl/workout/$encoded", style)
    }

    fun challengeView(style: IStyle): String = appendStyleParams("$appBaseUrl/challenge", style)
    fun trainerChatView(style: IStyle): String = appendStyleParams("$appBaseUrl/trainer", style)

    fun leaderboardView(username: String = "", style: IStyle): String {
        val baseUrl = if (username.isNotEmpty()) {
            "$appBaseUrl/leaderboard?username=$username"
        } else {
            "$appBaseUrl/leaderboard"
        }
        return appendStyleParams(baseUrl, style)
    }

    fun customWorkout(style: IStyle): String = appendStyleParams("$appBaseUrl/custom-workout", style)

    fun experienceView(experience: String, style: IStyle): String {
        val encoded = Uri.encode(experience.lowercase())
        return appendStyleParams("$appBaseUrl/experiences/$encoded", style)
    }

    fun personalizedPlanView(style: IStyle): String = appendStyleParams("$appBaseUrl/personalized-plan", style)

    fun adminView(
        contentType: ContentType?,
        contentId: String?,
        customQueries: Map<String, Any?>? = null
    ): String {
        val hasType = contentType != null
        val hasId = !contentId.isNullOrBlank()

        if (hasType != hasId) {
            Log.e("KinesteX", "contentType and contentId must be provided together")
            return adminBaseUrl
        }

        if (hasId && containsDisallowedCharacters(contentId!!)) {
            Log.e("KinesteX", "contentId contains disallowed characters")
            return adminBaseUrl
        }

        val pathSegments = if (hasType && hasId) {
            listOf(segmentFor(contentType), contentId)
        } else {
            listOf("main")
        }

        val uri = Uri.parse(adminBaseUrl).buildUpon()
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

    fun cameraView(style: IStyle): String = appendStyleParams("$appBaseUrl/camera", style)

    fun customComponentView(route: String, style: IStyle): String {
        val encodedPath = route.split("/").joinToString("/") { Uri.encode(it) }
        return appendStyleParams("$appBaseUrl/$encodedPath", style)
    }

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