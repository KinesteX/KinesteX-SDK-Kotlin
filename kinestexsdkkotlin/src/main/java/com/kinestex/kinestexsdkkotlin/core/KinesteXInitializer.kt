package com.kinestex.kinestexsdkkotlin.core

import android.content.Context

/**
 * Internal SDK initializer responsible for lifecycle management
 *
 * Handles SDK initialization, WebView warmup, and resource cleanup.
 * This class is used internally by [KinesteXSDK] and should not be used directly.
 *
 * @see KinesteXSDK.initialize
 */
class KinesteXInitializer {
    private val logger = KinesteXLogger.instance
    private var isInitialized = false

    /**
     * Initializes the SDK with authentication credentials
     *
     * Performs WebView warmup for faster first load and validates that
     * the SDK hasn't been initialized already.
     *
     * @param context Application context for resource access
     * @param apiKey API key for authentication
     * @param companyName Company identifier
     * @param userId Current user identifier
     */
    fun initialize(context: Context, apiKey: String, companyName: String, userId: String) {
        if (isInitialized) {
            logger.error("KinesteX SDK already initialized")
            return
        }

        logger.info("Initializing KinesteX SDK...")

        // Warmup the WebView controller
        GenericWebView.warmup(
            context = context.applicationContext,
            apiKey = apiKey,
            companyName = companyName,
            userId = userId
        )

        isInitialized = true
        logger.success("KinesteX SDK initialized with WebView warmup")
    }

    /**
     * Checks if the SDK has been initialized
     *
     * @return true if SDK is initialized, false otherwise
     */
    fun getIsInitialized(): Boolean = isInitialized

    /**
     * Disposes SDK resources and resets initialization state
     *
     * Cleans up the WebView controller and allows for reinitialization
     * if needed. Should be called when SDK is no longer needed.
     */
    fun dispose() {
        logger.info("Disposing KinesteX SDK...")

        // Dispose WebView controller
        GenericWebView.disposeWarmup()

        isInitialized = false
        logger.success("KinesteX SDK disposed")
    }
}