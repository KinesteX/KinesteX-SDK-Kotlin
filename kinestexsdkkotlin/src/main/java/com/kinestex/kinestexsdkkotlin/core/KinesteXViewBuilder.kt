package com.kinestex.kinestexsdkkotlin.core

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.models.Gender
import com.kinestex.kinestexsdkkotlin.models.IStyle
import com.kinestex.kinestexsdkkotlin.models.Lifestyle
import com.kinestex.kinestexsdkkotlin.models.PlanCategory
import com.kinestex.kinestexsdkkotlin.models.UserDetails
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Internal view builder for creating KinesteX WebView instances
 *
 * Provides centralized view creation with validation, error handling,
 * and proper data merging. Used internally by [KinesteXSDK] view creation methods.
 *
 * @see KinesteXSDK
 */
object KinesteXViewBuilder {
    private val logger = KinesteXLogger.instance

    /**
     * Builds a KinesteX WebView with the specified configuration
     *
     * Validates parameters, merges user details and custom parameters,
     * and creates a [GenericWebView] instance.
     *
     * @param context Activity or Fragment context
     * @param apiKey API key for authentication
     * @param companyName Company identifier
     * @param userId User identifier
     * @param url WebView URL to load
     * @param data View-specific data map
     * @param user Optional user details for personalization
     * @param customParams Optional custom parameters to merge
     * @param isLoading Loading state flow
     * @param permissionHandler Handler for permissions
     * @param onMessageReceived Callback for WebView messages
     * @return WebView instance or error view if validation fails
     */
    fun build(
        context: Context,
        apiKey: String,
        companyName: String,
        userId: String,
        url: String,
        data: Map<String, Any> = emptyMap(),
        user: UserDetails? = null,
        style: IStyle? = null,
        customParams: Map<String, Any>? = null,
        isLoading: MutableStateFlow<Boolean>,
        permissionHandler: PermissionHandler,
        onMessageReceived: (WebViewMessage) -> Unit
    ): View {
        // Step 1: Validate core parameters
        if (!validateCoreParams(apiKey, companyName, userId)) {
            logger.error("Core parameters validation failed")
            return createErrorView(context, "Invalid SDK configuration")
        }

        // Step 2: Build final data map
        // Note: Style params are now passed via URL query params, not in the data map
        val finalData = data.toMutableMap()
        addUserDetails(finalData, user)
        mergeCustomParams(finalData, customParams)

        logger.info("KinesteXViewBuilder: $apiKey - $companyName - $userId")

        // Step 3: Determine overlay color from IStyle
        val overlayColor = style?.loadingBackgroundColor?.let { colorFromHex(it) }
            ?: Color.BLACK

        // Step 4: Create and return WebView
        return GenericWebView(
            context = context,
            apiKey = apiKey,
            companyName = companyName,
            userId = userId,
            url = url,
            data = finalData,
            overlayColor = overlayColor,
            isLoading = isLoading,
            permissionHandler = permissionHandler,
            onMessageReceived = onMessageReceived
        )
    }

    private fun validateCoreParams(
        apiKey: String,
        company: String,
        userId: String
    ): Boolean {
        if (containsDisallowedCharacters(apiKey) ||
            containsDisallowedCharacters(company) ||
            containsDisallowedCharacters(userId)) {
            logger.error("Parameters contain disallowed characters")
            return false
        }
        return true
    }

    private fun addUserDetails(data: MutableMap<String, Any>, user: UserDetails?) {
        user?.let {
            data["age"] = it.age
            data["height"] = it.height
            data["weight"] = it.weight
            data["gender"] = genderString(it.gender)
            data["lifestyle"] = lifestyleString(it.lifestyle)
        }
    }

    private fun mergeCustomParams(
        data: MutableMap<String, Any>,
        customParams: Map<String, Any>?
    ) {
        customParams?.forEach { (key, value) ->
            // Validate key
            if (containsDisallowedCharacters(key)) {
                logger.error("Custom parameter key '$key' contains disallowed characters")
                return@forEach
            }

            // Validate string values
            if (value is String && containsDisallowedCharacters(value)) {
                logger.error("Custom parameter '$key' value contains disallowed characters")
                return@forEach
            }

            data[key] = value
        }
    }

    private fun createErrorView(context: Context, message: String): View {
        return TextView(context).apply {
            text = message
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
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

    // ============================================================
    // Helper functions for enum to string conversions
    // ============================================================

    /**
     * Converts PlanCategory enum to string representation
     */
    fun planCategoryString(category: PlanCategory): String {
        return when (category) {
            PlanCategory.Cardio -> "Cardio"
            PlanCategory.WeightManagement -> "Weight Management"
            PlanCategory.Strength -> "Strength"
            PlanCategory.Rehabilitation -> "Rehabilitation"
            is PlanCategory.Custom -> category.name
        }
    }

    /**
     * Converts Gender enum to string representation
     */
    fun genderString(gender: Gender): String {
        return when (gender) {
            Gender.MALE -> "Male"
            Gender.FEMALE -> "Female"
            Gender.UNKNOWN -> "Male"
        }
    }

    /**
     * Converts Lifestyle enum to string representation
     */
    fun lifestyleString(lifestyle: Lifestyle): String {
        return when (lifestyle) {
            Lifestyle.SEDENTARY -> "Sedentary"
            Lifestyle.SLIGHTLY_ACTIVE -> "Slightly Active"
            Lifestyle.ACTIVE -> "Active"
            Lifestyle.VERY_ACTIVE -> "Very Active"
        }
    }

    /**
     * Converts hex color string to Android Color int
     * Supports both #RGB and #RRGGBB formats
     */
    private fun colorFromHex(hex: String): Int {
        var cleanHex = hex.replace("#", "")

        // If only RGB (6 chars), add full opacity (FF)
        if (cleanHex.length == 6) {
            cleanHex = "FF$cleanHex"
        }

        return android.graphics.Color.parseColor("#$cleanHex")
    }
}