package com.kinestex.kinestexsdkkotlin.models

data class IStyle(
    val style: String? = null,
    val themeName: String? = null,
    val loadingStickmanColor: String? = null,
    val loadingBackgroundColor: String? = null,
    val loadingTextColor: String? = null
) {

    fun toJson(): Map<String, Any> {
        val data = mutableMapOf<String, Any>()

        style?.let { data["style"] = it }
        themeName?.let { data["themeName"] = it }
        loadingStickmanColor?.let { data["loadingStickmanColor"] = it }
        loadingBackgroundColor?.let { data["loadingBackgroundColor"] = it }
        loadingTextColor?.let { data["loadingTextColor"] = it }

        return data
    }
}
