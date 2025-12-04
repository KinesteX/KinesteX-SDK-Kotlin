package com.kinestex.kotlin_sdk.app

import android.app.Application
import com.kinestex.kinestexsdkkotlin.KinesteXSDK

class KinesteXApp : Application() {
    override fun onCreate() {
        super.onCreate()

        KinesteXSDK.initialize(
            context = this,
            apiKey = "13c5398cf7a98e3469f6fc8a9a5b2b9d5c8a4814",
            companyName = "KinesteX",
            userId = "GibOrTgo2KNuldDYrTARMnqba2T2"
        )
    }
}