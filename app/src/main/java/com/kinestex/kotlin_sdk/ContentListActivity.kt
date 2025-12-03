package com.kinestex.kotlin_sdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.kinestex.kinestexsdkkotlin.api.WorkoutModel
import com.kinestex.kinestexsdkkotlin.api.PlanModel
import com.kinestex.kinestexsdkkotlin.api.ExerciseModel
import com.kinestex.kinestexsdkkotlin.api.WorkoutsResponse
import com.kinestex.kinestexsdkkotlin.api.PlansResponse
import com.kinestex.kinestexsdkkotlin.api.ExerciseResponse
import com.kinestex.kotlin_sdk.ui.theme.KinesteXTheme

class ContentListActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.getStringExtra("type")
        val json = intent.getStringExtra("json") ?: return finish()

        val gson = Gson()

        setContent {
            KinesteXTheme {
                Scaffold(topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(type?.replaceFirstChar { it.uppercase() } ?: "Content") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }) { padding ->
                    when (type) {
                        "workouts" -> {
                            val response = gson.fromJson(json, WorkoutsResponse::class.java)
                            WorkoutsList(workouts = response.workouts, padding = padding)
                        }
                        "plans" -> {
                            val response = gson.fromJson(json, PlansResponse::class.java)
                            PlansList(plans = response.plans, padding = padding)
                        }
                        "exercises" -> {
                            val response = gson.fromJson(json, ExerciseResponse::class.java)
                            ExercisesList(exercises = response.exercises, padding = padding)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WorkoutsList(workouts: List<WorkoutModel>, padding: PaddingValues) {
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            items(workouts) { workout ->
                ListItemCard(
                    title = workout.title,
                    subtitle = "${workout.total_minutes ?: 0} min • ${workout.dif_level ?: "N/A"}",
                    imageUrl = workout.img_URL
                ) {
                    val intent = Intent(this@ContentListActivity, ContentDetailActivity::class.java).apply {
                        putExtra("type", "workout")
                        putExtra("json", Gson().toJson(workout))
                    }
                    startActivity(intent)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    @Composable
    private fun PlansList(plans: List<PlanModel>, padding: PaddingValues) {
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            items(plans) { plan ->
                val totalDays = plan.levels.values.sumOf { it.days.size }
                ListItemCard(
                    title = plan.title,
                    subtitle = "$totalDays days • ${plan.levels.size} levels",
                    imageUrl = plan.img_URL
                ) {
                    val intent = Intent(this@ContentListActivity, ContentDetailActivity::class.java).apply {
                        putExtra("type", "plan")
                        putExtra("json", Gson().toJson(plan))
                    }
                    startActivity(intent)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    @Composable
    private fun ExercisesList(exercises: List<ExerciseModel>, padding: PaddingValues) {
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            items(exercises) { exercise ->
                ListItemCard(
                    title = exercise.title,
                    subtitle = "Reps: ${exercise.avg_reps ?: "N/A"} • ${exercise.dif_level}",
                    imageUrl = exercise.thumbnail_URL
                ) {
                    val intent = Intent(this@ContentListActivity, ContentDetailActivity::class.java).apply {
                        putExtra("type", "exercise")
                        putExtra("json", Gson().toJson(exercise))
                    }
                    startActivity(intent)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ListItemCard(title: String, subtitle: String, imageUrl: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}