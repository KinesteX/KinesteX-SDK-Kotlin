package com.kinestex.kinestexsdkkotlin.core

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.models.UserDetails
import com.kinestex.kinestexsdkkotlin.models.WebViewMessage
import kotlinx.coroutines.flow.MutableStateFlow

object KinesteXViewBuilder {
    private val logger = KinesteXLogger.instance

    fun build(
        context: Context,
        apiKey: String,
        companyName: String,
        userId: String,
        url: String,
        data: Map<String, Any> = emptyMap(),
        user: UserDetails? = null,
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
        val finalData = data.toMutableMap()
        addUserDetails(finalData, user)
        mergeCustomParams(finalData, customParams)

        // Step 3: Create and return WebView
        return GenericWebView(
            context = context,
            apiKey = apiKey,
            companyName = companyName,
            userId = userId,
            url = url,
            data = finalData,
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
            data["gender"] = it.gender
            data["lifestyle"] = it.lifestyle
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
}