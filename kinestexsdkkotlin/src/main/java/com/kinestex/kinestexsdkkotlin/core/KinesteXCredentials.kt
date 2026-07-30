package com.kinestex.kinestexsdkkotlin.core

class KinesteXCredentials {
    // apiKey is optional: omitted for session-based auth (session id passed via
    // customParams instead). companyName/userId still gate initialization.
    private var apiKey: String? = null
    private var companyName: String? = null
    private var userId: String? = null

    fun set(apiKey: String?, companyName: String, userId: String) {
        this.apiKey = apiKey
        this.companyName = companyName
        this.userId = userId
    }

    fun get(): Credentials {
        return Credentials(
            apiKey = apiKey,
            companyName = companyName ?: throw IllegalStateException(
                "SDK not initialized. Call KinesteXSDK.initialize() first."
            ),
            userId = userId ?: throw IllegalStateException(
                "SDK not initialized. Call KinesteXSDK.initialize() first."
            )
        )
    }

    data class Credentials(
        val apiKey: String?,
        val companyName: String,
        val userId: String
    )
}