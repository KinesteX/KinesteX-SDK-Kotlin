package com.kinestex.kinestexsdkkotlin.models

data class IStyle(
    val style: String? = "dark",
    val themeName: String? = null,
    val loadingStickmanColor: String? = null,
    val loadingBackgroundColor: String? = null,
    val loadingTextColor: String? = null
) {
    fun toQueryParams(): Map<String, String> {
        val params = mutableMapOf<String, String>()

        style?.let { params["style"] = it }
        themeName?.let { params["themeName"] = it }
        loadingStickmanColor?.let { params["loadingStickmanColor"] = it }
        loadingBackgroundColor?.let { params["loadingBackgroundColor"] = it }
        loadingTextColor?.let { params["loadingTextColor"] = it }

        return params
    }
}
