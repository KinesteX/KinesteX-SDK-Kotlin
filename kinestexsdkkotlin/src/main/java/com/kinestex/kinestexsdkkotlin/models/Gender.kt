package com.kinestex.kinestexsdkkotlin.models

/**
 * Gender classification for calorie and exercise calculations
 *
 * Used to provide more accurate fitness metrics based on biological differences.
 */
enum class Gender {
    /** Male */
    MALE,

    /** Female */
    FEMALE,

    /** Unknown or prefer not to specify (defaults to MALE for calculations) */
    UNKNOWN
}