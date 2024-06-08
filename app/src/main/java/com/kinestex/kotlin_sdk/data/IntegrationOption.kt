package com.kinestex.kotlin_sdk.data
data class IntegrationOption(
    var title: String,
    var optionType: String? = null,
    var subOption: List<String>? = null
)

