package com.kinestex.kinestexsdkkotlin

import android.net.Uri
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class KinesteXAPI {
    companion object {
        private const val BASE_API_URL = "https://admin.kinestex.com/api/v1/"

        /**
         * Fetches content data from the API based on the provided parameters.
         *
         * @param apiKey The API key for authentication
         * @param companyName The name of the company making the request
         * @param contentType The type of content to fetch (workout, plan, or exercise)
         * @param id Optional unique identifier for the content
         * @param title Optional title to search for content
         * @param lang Language for the content (defaults to "en")
         * @return Result containing either the requested content or an error message
         */
        suspend fun fetchAPIContentData(
            apiKey: String,
            companyName: String,
            contentType: ContentType,
            id: String? = null,
            title: String? = null,
            lang: String = "en"
        ): APIContentResult {
            // Validation checks
            if (containsDisallowedCharacters(apiKey) ||
                containsDisallowedCharacters(companyName) ||
                containsDisallowedCharacters(lang)) {
                return APIContentResult.Error("⚠️ Validation Error: apiKey, companyName, or lang contains disallowed characters")
            }

            id?.let {
                if (containsDisallowedCharacters(it)) {
                    return APIContentResult.Error("⚠️ Error: ID contains disallowed characters")
                }
            }

            title?.let {
                if (containsDisallowedCharacters(it)) {
                    return APIContentResult.Error("⚠️ Error: Title contains disallowed characters")
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

            val url = Uri.parse(urlBuilder.toString()).buildUpon().apply {
                title?.let { appendQueryParameter("title", it) }
                appendQueryParameter("lang", lang)
            }.build().toString()


            return try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("x-company-name", companyName)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        try {
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
                        } catch (e: Exception) {
                            println("Failed to parse data: ${e.message}")
                            APIContentResult.Error("Failed to parse data: ${e.message}. Please contact us at support@kinestex.com if this issue persists")
                        }
                    } else {
                        try {
                            val errorResponse = Gson().fromJson(responseBody, APIResponse::class.java)
                            APIContentResult.Error("Error: ${errorResponse.message ?: errorResponse.error ?: "Unknown error"}")
                        } catch (e: Exception) {
                            APIContentResult.Error("Error ${response.code}: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                APIContentResult.Error("Network error: ${e}")
            }
        }

        private fun containsDisallowedCharacters(text: String): Boolean {
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
            return text.any { it in disallowedCharacters }
        }
    }
}