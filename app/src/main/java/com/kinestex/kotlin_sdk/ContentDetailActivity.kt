package com.kinestex.kotlin_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kinestex.kotlin_sdk.ui.theme.KinesteXTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.kinestex.kinestexsdkkotlin.api.WorkoutModel
import com.kinestex.kinestexsdkkotlin.api.PlanModel
import com.kinestex.kinestexsdkkotlin.api.ExerciseModel

class ContentDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("type")
        val json = intent.getStringExtra("json") ?: return finish()

        val gson = Gson()

        setContent {
            KinesteXTheme {
                when (type) {
                    "workout" -> {
                        val workout = gson.fromJson(json, WorkoutModel::class.java)
                        WorkoutDetailScreen(workout = workout, onBack = { finish() })
                    }
                    "plan" -> {
                        val plan = gson.fromJson(json, PlanModel::class.java)
                        PlanDetailScreen(plan = plan, onBack = { finish() })
                    }
                    "exercise" -> {
                        val exercise = gson.fromJson(json, ExerciseModel::class.java)
                        ExerciseDetailScreen(exercise = exercise, onBack = { finish() })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workout: WorkoutModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(workout.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                if (workout.img_URL.isNotEmpty()) {
                    AsyncImage(
                        model = workout.img_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(workout.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Duration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${workout.total_minutes ?: "—"} min", color = Color.Gray)
                    }
                    Column {
                        Text("Calories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${workout.total_calories ?: "—"} kcal", color = Color.Gray)
                    }
                    Column {
                        Text("Difficulty", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(workout.dif_level ?: "—", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(workout.description, color = Color.Gray)

                Spacer(Modifier.height(16.dp))

                Text("Targeted Body Parts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BodyPartsWrap(bodyParts = workout.body_parts)

                Spacer(Modifier.height(24.dp))

                Text("Exercise Sequence (${workout.sequence.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
            }

            workout.sequence.forEachIndexed { index, exercise ->
                item {
                    ExerciseSequenceCard(exercise = exercise, index = index + 1)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(plan: PlanModel, onBack: () -> Unit) {
    val expandedWeeks = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Plan Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                if (plan.img_URL.isNotEmpty()) {
                    AsyncImage(
                        model = plan.img_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(plan.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(plan.category.description)
                        Spacer(Modifier.height(8.dp))
                        Text("Category Levels:", fontWeight = FontWeight.SemiBold)
                        plan.category.levels.forEach { (key, value) ->
                            Text("$key: $value", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            plan.levels.toSortedMap().forEach { (levelKey, level) ->
                item {
                    val isExpanded = expandedWeeks[levelKey] ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { expandedWeeks[levelKey] = !isExpanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(level.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ArrowBack else Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.Blue
                                )
                            }

                            if (isExpanded) {
                                Spacer(Modifier.height(8.dp))
                                Text(level.description, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))

                                // Days
                                level.days.toSortedMap().forEach { (dayKey, day) ->
                                    PlanDayCard(day = day)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(exercise: ExerciseModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(exercise.title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                // Exercise Thumbnail
                if (exercise.thumbnail_URL.isNotEmpty()) {
                    AsyncImage(
                        model = exercise.thumbnail_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(16.dp))

                Text(exercise.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                DifficultyBadge(exercise.dif_level)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Reps", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${exercise.avg_reps ?: exercise.workout_reps ?: "—"}", color = Color.Gray)
                    }
                    Column {
                        Text("Countdown", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${exercise.avg_countdown ?: exercise.workout_countdown ?: "—"}s", color = Color.Gray)
                    }
                    Column {
                        Text("Rest", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${exercise.rest_duration ?: 0}s", color = Color.Gray)
                    }
                    Column {
                        Text("Calories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${exercise.avg_cal ?: "—"}", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Body Parts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BodyPartsWrap(bodyParts = exercise.body_parts)

                Spacer(Modifier.height(16.dp))

                Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(exercise.description, color = Color.Gray)

                Spacer(Modifier.height(16.dp))

                Text("Steps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                exercise.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("${index + 1}. ", fontWeight = FontWeight.SemiBold)
                        Text(step, fontSize = 14.sp, color = Color.Gray)
                    }
                }

                if (exercise.tips.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Tips", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(exercise.tips, fontSize = 14.sp, color = Color.Gray)
                }

                if (exercise.common_mistakes.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Common Mistakes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD32F2F))
                    Spacer(Modifier.height(4.dp))
                    Text(exercise.common_mistakes, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BodyPartsWrap(bodyParts: List<String>) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bodyParts.forEach { part ->
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = part,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DifficultyBadge(level: String) {
    val color = when (level.lowercase()) {
        "easy" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = level,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ExerciseSequenceCard(exercise: ExerciseModel, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val restDuration = exercise.rest_duration
            if (restDuration != null && restDuration > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Rest duration: $restDuration seconds",
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Exercise $index: ${exercise.title}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                DifficultyBadge(exercise.dif_level)
            }

            Spacer(Modifier.height(8.dp))

            // Thumbnails Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (exercise.thumbnail_URL.isNotEmpty()) {
                    AsyncImage(
                        model = exercise.thumbnail_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .height(400.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (exercise.workout_reps != null || exercise.avg_reps != null) {
                    Text(
                        "Reps: ${exercise.workout_reps ?: exercise.avg_reps}",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (exercise.workout_countdown != null || exercise.avg_countdown != null) {
                    Text(
                        "Countdown: ${exercise.workout_countdown ?: exercise.avg_countdown}",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Body Parts: ${exercise.body_parts.joinToString(", ")}", fontSize = 12.sp)

            Spacer(Modifier.height(8.dp))

            Text(exercise.description, fontSize = 14.sp)

            if (exercise.steps.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Steps:", fontWeight = FontWeight.Bold)
                exercise.steps.forEach { step ->
                    Text("• $step", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (exercise.tips.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Tips:", fontWeight = FontWeight.Bold)
                Text(exercise.tips, fontSize = 12.sp)
            }

            if (exercise.common_mistakes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Common Mistakes:", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Text(exercise.common_mistakes, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PlanDayCard(day: com.kinestex.kinestexsdkkotlin.api.PlanDay) {
    val isRestDay = day.title == "Rest today"
    val workoutsList = day.workouts

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRestDay) Color(0xFFFFF9C4) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                day.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRestDay) Color(0xFFF57C00) else Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(day.description, fontSize = 14.sp, color = Color.Gray)

            if (workoutsList != null && workoutsList.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                workoutsList.forEach { workout ->
                    WorkoutSummaryCard(workout = workout)
                }
            }
        }
    }
}

@Composable
fun WorkoutSummaryCard(workout: com.kinestex.kinestexsdkkotlin.api.WorkoutSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ID: ${workout.id}", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(workout.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Avg Cals: ${workout.calories ?: 0}", fontSize = 12.sp)
                Text("Total minutes: ${workout.total_minutes}", fontSize = 12.sp)
            }

            if (workout.imgURL.isNotEmpty()) {
                AsyncImage(
                    model = workout.imgURL,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}