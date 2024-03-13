package com.kinestex.kinestexsdkkotlin.models



sealed class PlanCategory {
    object Cardio : PlanCategory()
    object WeightManagement : PlanCategory()
    object Strength : PlanCategory()
    object Rehabilitation : PlanCategory()
    data class Custom(val description: String) : PlanCategory()
}