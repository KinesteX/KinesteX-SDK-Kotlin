package com.kinestex.kinestexsdkkotlin.api

import android.net.Uri
import com.google.gson.Gson
import com.kinestex.kinestexsdkkotlin.core.KinesteXLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

/**
 * KinesteX API client for fetching workout, plan, and exercise data
 *
 * @param apiKey API key for authentication
 * @param companyName Company identifier
 */
class KinesteXAPI(
    private val apiKey: String,
    private val companyName: String
) {
    private val logger = KinesteXLogger.instance

    companion object {
        private const val BASE_API_URL = "https://admin.kinestex.com/api/v1/"
        private const val TIMEOUT_SECONDS = 30L
    }

    /**
     * Reusable OkHttpClient with interceptors and timeout configuration
     */
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(createHeaderInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .build()
    }

    /**
     * Header injection interceptor
     * Automatically adds authentication headers to all requests
     */
    private fun createHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithHeaders = originalRequest.newBuilder()
                .addHeader("x-api-key", apiKey)
                .addHeader("x-company-name", companyName)
                .addHeader("Content-Type", "application/json")
                .build()

            chain.proceed(requestWithHeaders)
        }
    }

    /**
     * Logging interceptor
     * Logs all API requests and responses for debugging
     */
    private fun createLoggingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            // Log request
            logger.info("API Request: ${request.method} ${request.url}")

            try {
                val response = chain.proceed(request)

                // Log response
                val responseLog = "API Response: ${response.code} ${request.url}"
                if (response.isSuccessful) {
                    logger.success(responseLog)
                } else {
                    logger.error("$responseLog - Status: ${response.message}")
                }

                response
            } catch (e: Exception) {
                logger.error("API Request Failed: ${request.url}", e)
                throw e
            }
        }
    }

    /**
     * Fetches content data from the API based on the provided parameters.
     *
     * @param contentType The type of content to fetch (workout, plan, or exercise)
     * @param id Optional unique identifier for the content
     * @param title Optional title to search for content
     * @param lang Language for the content (defaults to "en")
     * @param category Optional category filter
     * @param lastDocId Optional last document ID for pagination
     * @param limit Optional limit for number of results
     * @param bodyParts Optional list of body parts to filter by
     * @return Result containing either the requested content or an error message
     */
    suspend fun fetchAPIContentData(
        contentType: ContentType,
        id: String? = null,
        title: String? = null,
        lang: String = "en",
        category: String? = null,
        lastDocId: String? = null,
        limit: Int? = null,
        bodyParts: List<BodyPart>? = null
    ): APIContentResult = withContext(Dispatchers.IO) {
            // Validation checks
            if (containsDisallowedCharacters(apiKey) ||
                containsDisallowedCharacters(companyName) ||
                containsDisallowedCharacters(lang)) {
                return@withContext APIContentResult.Error("⚠️ Validation Error: apiKey, companyName, or lang contains disallowed characters")
            }

            id?.let {
                if (containsDisallowedCharacters(it)) {
                    return@withContext APIContentResult.Error("⚠️ Error: ID contains disallowed characters")
                }
            }

            title?.let {
                if (containsDisallowedCharacters(it)) {
                    return@withContext APIContentResult.Error("⚠️ Error: Title contains disallowed characters")
                }
            }

            category?.let {
                if (containsDisallowedCharacters(it)) {
                    return@withContext APIContentResult.Error("⚠️ Error: Category contains disallowed characters")
                }
            }

            lastDocId?.let {
                if (containsDisallowedCharacters(it)) {
                    return@withContext APIContentResult.Error("⚠️ Error: LastDocID contains disallowed characters")
                }
            }

            // Determine endpoint
            val endpoint = when (contentType) {
                ContentType.WORKOUT -> "workouts"
                ContentType.PLAN -> "plans"
                ContentType.EXERCISE -> "exercises"
            }

            // Construct URL
            val urlBuilder = StringBuilder(BASE_API_URL).append(endpoint)
            id?.let { urlBuilder.append("/$it") }
            title?.let { urlBuilder.append("/$it") }

            val url = urlBuilder.toString().toUri().buildUpon().apply {
                appendQueryParameter("lang", lang)
                category?.let { appendQueryParameter("category", it) }
                lastDocId?.let { appendQueryParameter("lastDocId", it) }
                limit?.let { appendQueryParameter("limit", it.toString()) }
                bodyParts?.let {
                    val bodyPartsString = it.joinToString(",") { part -> part.value }
                    appendQueryParameter("body_parts", bodyPartsString)
                }
            }.build().toString()

            logger.debug("Fetching content: $contentType from $url")

            return@withContext try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        try {
                            if (category != null || bodyParts != null || lastDocId != null) {
                                // Handle array responses
                                when (contentType) {
                                    ContentType.WORKOUT -> {
                                        val workouts = DataProcessor.processWorkoutsArray(responseBody)
                                        APIContentResult.Workouts(workouts)
                                    }
                                    ContentType.PLAN -> {
                                        val plans = DataProcessor.processPlansArray(responseBody)
                                        APIContentResult.Plans(plans)
                                    }
                                    ContentType.EXERCISE -> {
                                        val exercises = DataProcessor.processExercisesArray(responseBody)
                                        APIContentResult.Exercises(exercises)
                                    }
                                }
                            } else {
                                // Handle single item responses
                                when (contentType) {
                                    ContentType.WORKOUT -> {
                                        val workout = DataProcessor.processWorkoutData(responseBody)
                                        APIContentResult.Workout(workout)
                                    }
                                    ContentType.PLAN -> {
                                        val plan = DataProcessor.processPlanData(responseBody)
                                        APIContentResult.Plan(plan)
                                    }
                                    ContentType.EXERCISE -> {
                                        val exercise = DataProcessor.processExerciseData(responseBody)
                                        APIContentResult.Exercise(exercise)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("Failed to parse API response data", e)
                            APIContentResult.Error("Failed to parse data: ${e.message}. Please contact us at support@kinestex.com if this issue persists")
                        }
                    } else {
                        try {
                            val errorResponse = Gson().fromJson(responseBody, APIResponse::class.java)
                            val errorMsg = "Error: ${errorResponse.message ?: errorResponse.error ?: "Unknown error"}"
                            logger.error(errorMsg)
                            APIContentResult.Error(errorMsg)
                        } catch (e: Exception) {
                            logger.error("API request failed with code ${response.code}", e)
                            APIContentResult.Error("Error ${response.code}: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Network error during API request", e)
                APIContentResult.Error("Network error: ${e.message}")
            }
    }

    /**
     * Validates text for disallowed characters that could pose security risks
     */
    private fun containsDisallowedCharacters(text: String): Boolean {
        val disallowedCharacters = setOf(
            '<', '>', '{', '}', '(', ')', '[', ']', ';', '"', '\'',
            '$', '.', '#', '`'
        )
        return text.any { it in disallowedCharacters }
    }
}