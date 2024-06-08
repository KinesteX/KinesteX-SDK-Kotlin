package com.kinestex.kinestexsdkkotlin

sealed class PlanCategory {
    object Cardio : PlanCategory()
    object WeightManagement : PlanCategory()
    object Strength : PlanCategory()
    object Rehabilitation : PlanCategory()
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