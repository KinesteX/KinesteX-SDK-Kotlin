package com.kinestex.kinestexsdkkotlin.models

/**
 * Activity level classification for exercise personalization
 *
 * Helps adjust workout difficulty and recommendations based on the user's
 * typical daily activity level.
 */
enum class Lifestyle {
    /** Little to no exercise, desk job */
    SEDENTARY,

    /** Light exercise 1-3 days per week */
    SLIGHTLY_ACTIVE,

    /** Moderate exercise 3-5 days per week */
    ACTIVE,

    /** Hard exercise 6-7 days per week */
    VERY_ACTIVE
}