package com.kinestex.kotlin_sdk.data

enum class IntegrationOptionType(
    val title: String, val category: String, val subOptions: List<String>?
) {
    COMPLETE_UX(
        "Complete UX",
        "Goal Category",
        listOf("Cardio", "Strength", "Rehabilitation", "WeightManagement")
    ),
    WORKOUT_PLAN(
        "Workout Plan",
        "Plan",
        listOf("Full Cardio", "Elastic Evolution", "Circuit Training", "Fitness Cardio")
    ),
    WORKOUT(
        "Workout", "Workout", listOf("Fitness Lite", "Circuit Training", "Tabata")
    ),
    CHALLENGE(
        "Challenge", "Challenge", listOf("Squats", "Jumping Jack")
    ),
    CAMERA(
        "Camera", "", null
    ),
    EXPERIENCE(
        "Experience", "Experience", listOf("Box")
    ),
    LEADERBOARD(
        "Leaderboard", "", null
    );

    companion object {
        fun fromPosition(position: Int): IntegrationOptionType? {
            return entries.getOrNull(position)
        }
    }
}