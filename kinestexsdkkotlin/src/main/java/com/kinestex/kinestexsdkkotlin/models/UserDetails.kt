package com.kinestex.kinestexsdkkotlin.models

/**
 * User biometric and demographic details for workout personalization
 *
 * Providing user details enables the SDK to:
 * - Adjust exercise difficulty and recommendations
 * - Calculate accurate calorie estimates
 * - Provide personalized workout plans
 *
 * Note: User details are only used on-device for session customization.
 * To persist customization across sessions, pass these details each time.
 *
 * @param age User's age in years
 * @param height User's height in centimeters
 * @param weight User's weight in kilograms
 * @param gender User's gender for calorie calculations
 * @param lifestyle User's activity level for exercise adjustments
 */
data class UserDetails(
    val age: Int,
    val height: Int,
    val weight: Int,
    val gender: Gender,
    val lifestyle: Lifestyle
)