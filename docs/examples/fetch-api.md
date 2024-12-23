### FetchContentAPI example

```kotlin
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kinestex.kinestexsdkkotlin.APIContentResult
import com.kinestex.kinestexsdkkotlin.BodyPart
import com.kinestex.kinestexsdkkotlin.ContentType
import com.kinestex.kinestexsdkkotlin.KinesteXAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val apiKey = "YOUR API KEY"
    private val company = "YOUR COMPANY NAME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFetchContent = findViewById<Button>(R.id.btnFetchContent)
        btnFetchContent.setOnClickListener {
            fetchContent()
        }
    }

    private fun fetchContent() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    KinesteXAPI.fetchAPIContentData(
                        apiKey = apiKey,
                        companyName = company,
                        contentType = ContentType.WORKOUT,
                        category = "Fitness",
                        bodyParts = listOf(BodyPart.ABS),
                        limit = 5
                    )
                }
                handleAPIResult(result)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleAPIResult(result: APIContentResult) {
        when (result) {
            is APIContentResult.Workout -> {
                val workout = result.workout
                // Handle single workout data
                println("Workout Title: ${workout.title}")
                println("Body Parts: ${workout.body_parts.joinToString { it.value }}")
            }
            is APIContentResult.Plan -> {
                val plan = result.plan
                // Handle single plan data
                println("Plan Title: ${plan.title}")
                println("Categories: ${plan.category.description}")
            }
            is APIContentResult.Exercise -> {
                val exercise = result.exercise
                // Handle single exercise data
                println("Exercise Title: ${exercise.title}")
                println("Body Parts: ${exercise.body_parts.joinToString { it.value }}")
            }
            is APIContentResult.Workouts -> {
                val workouts = result.workouts.workouts
                // Handle list of workouts
                workouts.forEach { workout ->
                    println("Workout Title: ${workout.title}")
                    println("Body Parts: ${workout.body_parts.joinToString { it.value }}")
                }
                // Use result.workouts.lastDocId for pagination
            }
            is APIContentResult.Plans -> {
                val plans = result.plans.plans
                // Handle list of plans
                plans.forEach { plan ->
                    println("Plan Title: ${plan.title}")
                    println("Categories: ${plan.category.description}")
                }
                // Use result.plans.lastDocId for pagination
            }
            is APIContentResult.Exercises -> {
                val exercises = result.exercises.exercises
                // Handle list of exercises
                exercises.forEach { exercise ->
                    println("Exercise Title: ${exercise.title}")
                    println("Body Parts: ${exercise.body_parts.joinToString { it.value }}")
                }
                // Use result.exercises.lastDocId for pagination
            }
            is APIContentResult.Error -> {
                // Handle error
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
```
