package com.kinestex.kotlin_sdk.app

import android.app.Application
import com.kinestex.kinestexsdkkotlin.KinesteXSDK

class KinesteXApp : Application() {
    override fun onCreate() {
        super.onCreate()

        KinesteXSDK.initialize(
            context = this,
            apiKey = "your-api-key",
            companyName = "your-company",
            userId = "your-user-id"
        )
    }
}