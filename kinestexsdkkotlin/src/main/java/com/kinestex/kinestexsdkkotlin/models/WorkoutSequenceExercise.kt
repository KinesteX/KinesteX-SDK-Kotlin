package com.kinestex.kinestexsdkkotlin.models

data class WorkoutSequenceExercise(
    val exerciseId: String,
    val reps: Int? = null,
    val duration: Int? = null,
    val includeRestPeriod: Boolean = false,
    val restDuration: Int = 0
) {

    fun copyWith(
        exerciseId: String? = null,
        reps: Int? = null,
        duration: Int? = null,
        includeRestPeriod: Boolean? = null,
        restDuration: Int? = null
    ): WorkoutSequenceExercise {
        return WorkoutSequenceExercise(
            exerciseId = exerciseId ?: this.exerciseId,
            reps = reps ?: this.reps,
            duration = duration ?: this.duration,
            includeRestPeriod = includeRestPeriod ?: this.includeRestPeriod,
            restDuration = restDuration ?: this.restDuration
        )
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "exerciseId" to exerciseId,
            "reps" to reps,
            "duration" to duration,
            "includeRestPeriod" to includeRestPeriod,
            "restDuration" to restDuration
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSequenceExercise {
            return WorkoutSequenceExercise(
                exerciseId = map["exerciseId"] as String,
                reps = (map["reps"] as? Number)?.toInt(),
                duration = (map["duration"] as? Number)?.toInt(),
                includeRestPeriod = map["includeRestPeriod"] as? Boolean ?: false,
                restDuration = (map["restDuration"] as? Number)?.toInt() ?: 0
            )
        }

        fun fromJson(jsonString: String): WorkoutSequenceExercise {
            val obj = kotlinx.serialization.json.Json.decodeFromString<Map<String, Any?>>(jsonString)
            return fromMap(obj)
        }
    }
}
