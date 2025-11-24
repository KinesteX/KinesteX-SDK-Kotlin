package com.kinestex.kinestexsdkkotlin.core

import android.content.Context

class KinesteXInitializer {
    private var isInitialized = false
    private val logger = KinesteXLogger.instance

    fun initialize(
        context: Context,
        apiKey: String,
        companyName: String,
        userId: String
    ) {
        if (isInitialized) {
            logger.info("SDK already initialized")
            return
        }

        logger.info("Initializing SDK...")

        // Validate credentials
        require(apiKey.isNotBlank()) { "API key cannot be empty" }
        require(companyName.isNotBlank()) { "Company name cannot be empty" }
        require(userId.isNotBlank()) { "User ID cannot be empty" }

        // Validate security
        require(!containsDisallowedCharacters(apiKey)) {
            "API key contains disallowed characters"
        }
        require(!containsDisallowedCharacters(companyName)) {
            "Company name contains disallowed characters"
        }
        require(!containsDisallowedCharacters(userId)) {
            "User ID contains disallowed characters"
        }

        // Warmup WebView for faster first load
        GenericWebView.warmup(context, apiKey, companyName, userId)

        isInitialized = true
        logger.success("SDK initialized successfully")
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

    fun dispose() {
        GenericWebView.disposeWarmup()
        isInitialized = false
        logger.info("SDK disposed")
    }

    fun getIsInitialized(): Boolean = isInitialized
}