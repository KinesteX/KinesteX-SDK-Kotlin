package com.kinestex.kinestexsdkkotlin.models

/**
 * Workout plan categories for filtering and selection
 *
 * Represents different types of workout plans available in KinesteX.
 * Use predefined categories or create custom ones.
 */
sealed class PlanCategory {
    /** Cardio-focused workout plans */
    object Cardio : PlanCategory()

    /** Weight management and loss plans */
    object WeightManagement : PlanCategory()

    /** Strength training and muscle building plans */
    object Strength : PlanCategory()

    /** Rehabilitation and recovery plans */
    object Rehabilitation : PlanCategory()

    /**
     * Custom plan category
     * @param name Custom category name
     */
    data class Custom(val name: String) : PlanCategory()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanCategory) return false

        return when (this) {
            is Cardio -> other is Cardio
            is WeightManagement -> other is WeightManagement
            is Strength -> other is Strength
            is Rehabilitation -> other is Rehabilitation
            is Custom -> other is Custom && this.name == other.name
        }
    }

    override fun hashCode(): Int {
        return when (this) {
            is Cardio -> Cardio::class.hashCode()
            is WeightManagement -> WeightManagement::class.hashCode()
            is Strength -> Strength::class.hashCode()
            is Rehabilitation -> Rehabilitation::class.hashCode()
            is Custom -> name.hashCode()
        }
    }

}