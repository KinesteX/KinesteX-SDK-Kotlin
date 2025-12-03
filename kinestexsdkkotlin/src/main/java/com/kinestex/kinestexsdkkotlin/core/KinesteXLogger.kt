package com.kinestex.kinestexsdkkotlin.core

import android.util.Log

object KinesteXLogger {
    private const val TAG = "KinesteX"
    val instance: KinesteXLogger = this
    var isEnabled = true

    fun info(message: String) {
        if (isEnabled) Log.i(TAG, "ℹ️ $message")
    }

    fun success(message: String) {
        if (isEnabled) Log.i(TAG, "✅ $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e(TAG, "⚠️ $message", throwable)
            } else {
                Log.e(TAG, "⚠️ $message")
            }
        }
    }

    fun debug(message: String) {
        if (isEnabled) Log.d(TAG, "🔍 $message")
    }
}