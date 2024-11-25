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
         * @param category Optional category filter
         * @param lastDocId Optional last document ID for pagination
         * @param limit Optional limit for number of results
         * @param bodyParts Optional list of body parts to filter by
         * @return Result containing either the requested content or an error message
         */
        suspend fun fetchAPIContentData(
            apiKey: String,
            companyName: String,
            contentType: ContentType,
            id: String? = null,
            title: String? = null,
            lang: String = "en",
            category: String? = null,
            lastDocId: String? = null,
            limit: Int? = null,
            bodyParts: List<BodyPart>? = null
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

            category?.let {
                if (containsDisallowedCharacters(it)) {
                    return APIContentResult.Error("⚠️ Error: Category contains disallowed characters")
                }
            }

            lastDocId?.let {
                if (containsDisallowedCharacters(it)) {
                    return APIContentResult.Error("⚠️ Error: LastDocID contains disallowed characters")
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

            val url = Uri.parse(urlBuilder.toString()).buildUpon().apply {
                appendQueryParameter("lang", lang)
                category?.let { appendQueryParameter("category", it) }
                lastDocId?.let { appendQueryParameter("lastDocId", it) }
                limit?.let { appendQueryParameter("limit", it.toString()) }
                bodyParts?.let {
                    val bodyPartsString = it.joinToString(",") { part -> part.value }
                    appendQueryParameter("body_parts", bodyPartsString)
                }
            }.build().toString()
            println("URL: $url")
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
                '<', '>', '{', '}', '(', ')', '[', ']', ';', '"', '\'',
                '$', '.', '#', '<', '>', '`'
            )
            return text.any { it in disallowedCharacters }
        }
    }
}