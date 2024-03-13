package com.kinestex.kinestexsdkkotlin.models

sealed class WorkoutCategory {
    object Fitness : WorkoutCategory()
    object Rehabilitation : WorkoutCategory()
    data class Custom(val description: String) : WorkoutCategory()
}