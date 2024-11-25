package com.kinestex.kinestexsdkkotlin

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// API Result sealed class for handling different response types
sealed class APIContentResult {
    data class Workouts(val workouts: WorkoutsResponse) : APIContentResult()
    data class Workout(val workout: WorkoutModel) : APIContentResult()
    data class Plans(val plans: PlansResponse) : APIContentResult()
    data class Plan(val plan: PlanModel) : APIContentResult()
    data class Exercises(val exercises: ExerciseResponse) : APIContentResult()
    data class Exercise(val exercise: ExerciseModel) : APIContentResult()
    data class Error(val message: String) : APIContentResult()
}

data class WorkoutsResponse(
    val workouts: List<WorkoutModel>,
    val lastDocId: String
)

data class ExerciseResponse(
    val exercises: List<ExerciseModel>,
    val lastDocId: String
)

data class PlansResponse(
    val plans: List<PlanModel>,
    val lastDocId: String
)

enum class BodyPart(val value: String) {
    ABS("Abs"),
    BICEPS("Biceps"),
    CALVES("Calves"),
    CHEST("Chest"),
    EXTERNAL_OBLIQUE("External Oblique"),
    FOREARMS("Forearms"),
    GLUTES("Glutes"),
    NECK("Neck"),
    QUADS("Quads"),
    SHOULDERS("Shoulders"),
    TRICEPS("Triceps"),
    HAMSTRINGS("Hamstrings"),
    LATS("Lats"),
    LOWER_BACK("Lower Back"),
    TRAPS("Traps"),
    FULL_BODY("Full Body");

    companion object {
        fun fromValue(value: String): BodyPart? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}



// Models
data class WorkoutModel(
    val id: String,
    val title: String,
    val img_URL: String,
    val category: String?,
    val description: String,
    val total_minutes: Int?,
    val total_calories: Int?,
    val body_parts: List<String>,
    val dif_level: String?,
    val sequence: List<ExerciseModel>
)

data class ExerciseModel(
    val id: String,
    val title: String,
    val thumbnail_URL: String,
    val video_URL: String,
    val workout_countdown: Int?,
    val workout_reps: Int?,
    val avg_reps: Int?,
    val avg_countdown: Int?,
    val rest_duration: Int?,
    val avg_cal: Double?,
    val body_parts: List<String>,
    val description: String,
    val dif_level: String,
    val common_mistakes: String,
    val steps: List<String>,
    val tips: String
)

data class PlanModel(
    val id: String,
    val img_URL: String,
    val title: String,
    val category: PlanModelCategory,
    val levels: Map<String, PlanLevel>,
)

data class PlanModelCategory(
    val description: String,
    val levels: Map<String, Int>
)

data class PlanLevel(
    val title: String,
    val description: String,
    val days: Map<String, PlanDay>
)

data class PlanDay(
    val title: String,
    val description: String,
    val workouts: List<WorkoutSummary>?
)

data class WorkoutSummary(
    val title: String,
    val id: String,
    val imgURL: String,
    val calories: Double?,
    val total_minutes: Int?
)

enum class ContentType(val value: String) {
    WORKOUT("Workout"),
    PLAN("Plan"),
    EXERCISE("Exercise")
}

// API Response model
data class APIResponse(
    val message: String?,
    val error: String?
)

// Raw data models for JSON parsing
private data class RawWorkoutData(
    val id: String,
    val body_img: String?,
    val workout_desc_img: String,
    val calories: Int?,
    val category: String?,
    val title: String,
    val total_minutes: Int?,
    val description: String,
    val dif_level: String?,
    val body_parts: List<String>,
    val sequence: List<RawSequenceItem>
)

private data class RawSequenceItem(
    val id: String?,
    val title: String,
    val countdown: Int?,
    val repeats: Int?,
    val correct_second: Double?,
    val video_URL: String?,
    val thumbnail_URL: String?,
    val calories: Double?,
    val body_parts: List<String>?,
    val dif_level: String?,
    val description: String?,
    val steps: List<String?>?,
    val tips: String?,
    val common_mistakes: String?,
    val workout_repeats: Int?,
    val workout_countdown: Int?
)

private data class WorkoutsResponseRaw(
    val workouts: List<RawWorkoutData>,
    val lastDocId: String
)

private data class ExerciseResponseRaw(
    val exercises: List<RawSequenceItem>,
    val lastDocId: String
)

// Data processing utility object
object DataProcessor {
    private val gson = Gson()

    @Throws(Exception::class)
    fun processPlanData(data: String): PlanModel {
        return try {
            gson.fromJson(data, PlanModel::class.java)
        } catch (e: JsonSyntaxException) {
            println("Error parsing Plan data: ${e.message}")
            throw e
        }
    }


    // Process array of Workouts
    @Throws(Exception::class)
    fun processWorkoutsArray(data: String): WorkoutsResponse {
        return try {
            val workoutsResponseRaw = gson.fromJson(data, WorkoutsResponseRaw::class.java)
            val workoutModels = workoutsResponseRaw.workouts.map { rawWorkout ->
                WorkoutModel(
                    id = rawWorkout.id,
                    title = rawWorkout.title,
                    img_URL = rawWorkout.workout_desc_img,
                    category = rawWorkout.category,
                    description = rawWorkout.description,
                    total_minutes = rawWorkout.total_minutes,
                    total_calories = rawWorkout.calories,
                    body_parts = rawWorkout.body_parts,
                    dif_level = rawWorkout.dif_level,
                    sequence = processSequence(rawWorkout.sequence)
                )
            }
            WorkoutsResponse(workouts = workoutModels, lastDocId = workoutsResponseRaw.lastDocId)
        } catch (e: JsonSyntaxException) {
            println("Error parsing Workouts array: ${e.message}")
            throw e
        }
    }

    // Process array of Exercises
    @Throws(Exception::class)
    fun processExercisesArray(data: String): ExerciseResponse {
        return try {
            val exerciseResponseRaw = gson.fromJson(data, ExerciseResponseRaw::class.java)
            val exercises = exerciseResponseRaw.exercises.map { item ->
                ExerciseModel(
                    id = item.id ?: "NA",
                    title = item.title,
                    thumbnail_URL = item.thumbnail_URL ?: "",
                    video_URL = item.video_URL ?: "",
                    workout_countdown = item.workout_countdown,
                    workout_reps = item.workout_repeats,
                    avg_reps = item.repeats,
                    avg_countdown = item.countdown,
                    rest_duration = 10,
                    avg_cal = item.calories,
                    body_parts = item.body_parts ?: emptyList(),
                    description = item.description ?: "Missing exercise description",
                    dif_level = item.dif_level ?: "Medium",
                    common_mistakes = item.common_mistakes ?: "",
                    steps = processSteps(item.steps),
                    tips = item.tips ?: ""
                )
            }
            ExerciseResponse(exercises = exercises, lastDocId = exerciseResponseRaw.lastDocId)
        } catch (e: JsonSyntaxException) {
            println("Error parsing Exercises array: ${e.message}")
            throw e
        }
    }

    // Process array of Plans
    @Throws(Exception::class)
    fun processPlansArray(data: String): PlansResponse {
        return try {
            val plansResponse = gson.fromJson(data, PlansResponse::class.java)
            plansResponse
        } catch (e: JsonSyntaxException) {
            println("Error parsing Plans array: ${e.message}")
            throw e
        }
    }

    @Throws(Exception::class)
    fun processWorkoutData(data: String): WorkoutModel {
        return try {
            val rawWorkout = gson.fromJson(data, RawWorkoutData::class.java)
            WorkoutModel(
                id = rawWorkout.id,
                title = rawWorkout.title,
                img_URL = rawWorkout.workout_desc_img,
                category = rawWorkout.category,
                description = rawWorkout.description,
                total_minutes = rawWorkout.total_minutes,
                total_calories = rawWorkout.calories,
                body_parts = rawWorkout.body_parts,
                dif_level = rawWorkout.dif_level,
                sequence = processSequence(rawWorkout.sequence)
            )
        } catch (e: JsonSyntaxException) {
            println("Error parsing Workout data: ${e.message}")
            throw e
        }
    }

    @Throws(Exception::class)
    fun processExerciseData(data: String): ExerciseModel {
        return try {
            val item = gson.fromJson(data, RawSequenceItem::class.java)
            ExerciseModel(
                id = item.id ?: "NA",
                title = item.title,
                thumbnail_URL = item.thumbnail_URL ?: "",
                video_URL = item.video_URL ?: "",
                workout_countdown = item.workout_countdown,
                workout_reps = item.workout_repeats,
                avg_reps = item.repeats,
                avg_countdown = item.countdown,
                rest_duration = 10,
                avg_cal = item.calories,
                body_parts = item.body_parts ?: emptyList(),
                description = item.description ?: "Missing exercise description",
                dif_level = item.dif_level ?: "Medium",
                common_mistakes = item.common_mistakes ?: "",
                steps = processSteps(item.steps),
                tips = item.tips ?: ""
            )
        } catch (e: JsonSyntaxException) {
            println("Error parsing Exercise data: ${e.message}")
            throw e
        }
    }

    private fun processSequence(sequence: List<RawSequenceItem>): List<ExerciseModel> {
        var currentRestDuration = 0
        return sequence.mapNotNull { item ->
            if (item.title == "Rest") {
                currentRestDuration = item.countdown ?: 10
                null
            } else {
                ExerciseModel(
                    id = item.id ?: "NA",
                    title = item.title,
                    thumbnail_URL = item.thumbnail_URL ?: "",
                    video_URL = item.video_URL ?: "",
                    workout_countdown = item.workout_countdown,
                    workout_reps = item.workout_repeats,
                    avg_reps = item.workout_repeats,
                    avg_countdown = item.repeats,
                    rest_duration = currentRestDuration,
                    avg_cal = item.calories,
                    body_parts = item.body_parts ?: emptyList(),
                    description = item.description ?: "Missing exercise description",
                    dif_level = item.dif_level ?: "Medium",
                    common_mistakes = item.common_mistakes ?: "",
                    steps = processSteps(item.steps),
                    tips = item.tips ?: ""
                ).also { currentRestDuration = 0 }
            }
        }
    }

    private fun processSteps(steps: List<String?>?): List<String> {
        return steps?.mapNotNull { it }?.filter { it.isNotEmpty() } ?: emptyList()
    }
}
