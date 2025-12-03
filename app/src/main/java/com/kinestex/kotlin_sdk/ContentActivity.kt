package com.kinestex.kotlin_sdk

import android.content.Intent
import androidx.activity.compose.setContent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.api.APIContentResult
import com.kinestex.kinestexsdkkotlin.api.APIContentResult.Exercise
import com.kinestex.kinestexsdkkotlin.api.APIContentResult.Plan
import com.kinestex.kinestexsdkkotlin.api.APIContentResult.Workout
import com.kinestex.kinestexsdkkotlin.api.BodyPart
import com.kinestex.kinestexsdkkotlin.api.ContentType
import com.kinestex.kotlin_sdk.ui.theme.KinesteXTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContentActivity : ComponentActivity() {

    private var selectedContent by mutableStateOf(ContentType.WORKOUT)
    private var selectedFilter by mutableStateOf(FilterType.NONE)
    private var selectedSearchType by mutableStateOf(SearchType.BY_ID)
    private val selectedBodyParts = mutableStateListOf<BodyPart>()
    private var searchText by mutableStateOf("")
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KinesteXTheme {
                ContentSelectionScreen(
                    selectedContent = selectedContent,
                    onContentChange = { selectedContent = it },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    selectedSearchType = selectedSearchType,
                    onSearchTypeChange = { selectedSearchType = it },
                    selectedBodyParts = selectedBodyParts,
                    onBodyPartToggle = { part ->
                        if (selectedBodyParts.contains(part)) {
                            selectedBodyParts.remove(part)
                        } else {
                            selectedBodyParts.add(part)
                        }
                    },
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    isLoading = isLoading,
                    onFetchContent = {
                        lifecycleScope.launch {
                            try {
                                isLoading = true
                                val result = withContext(Dispatchers.IO) {
                                    var id: String? = null
                                    var title: String? = null
                                    var category: String? = null
                                    var bodyParts: List<BodyPart>? = null


                                    if (selectedFilter == FilterType.NONE) {
                                        if (selectedSearchType == SearchType.BY_ID) {
                                            id = if (searchText.isNotEmpty()) searchText else null
                                        } else if (selectedSearchType == SearchType.BY_TITLE) {
                                            title =
                                                if (searchText.isNotEmpty()) searchText else null
                                        }
                                    }

                                    if (selectedFilter == FilterType.CATEGORY) {
                                        category = if (searchText.isNotEmpty()) searchText else null
                                    }

                                    if (selectedFilter == FilterType.BODY_PARTS) {
                                        bodyParts =
                                            if (selectedBodyParts.isNotEmpty()) selectedBodyParts else null
                                    }

                                    fetchContent(
                                        id = id,
                                        contentType = selectedContent,
                                        category = category,
                                        title = title,
                                        bodyParts = bodyParts
                                    )
                                }

                                handleAPIResult(result)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@ContentActivity,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }

    private suspend fun fetchContent(
        contentType: ContentType,
        id: String? = null, title: String? = null,
        bodyParts: List<BodyPart>? = null, lastDocId: String? = null,
        category: String? = null, limit: Int? = null
    ): APIContentResult {
        return KinesteXSDK.api.fetchAPIContentData(
            contentType = contentType,
            title = title,
            id = id,
            bodyParts = bodyParts,
            lastDocId = lastDocId,
            category = category,
            limit = limit
        )
    }

    private fun handleAPIResult(result: APIContentResult) {
        val gson = Gson()

        when (result) {
            is Workout -> {
                val intent = Intent(this, ContentDetailActivity::class.java).apply {
                    putExtra("type", "workout")
                    putExtra("json", gson.toJson(result.workout))
                }
                startActivity(intent)
            }

            is Plan -> {
                val intent = Intent(this, ContentDetailActivity::class.java).apply {
                    putExtra("type", "plan")
                    putExtra("json", gson.toJson(result.plan))
                }
                startActivity(intent)
            }

            is Exercise -> {
                val intent = Intent(this, ContentDetailActivity::class.java).apply {
                    putExtra("type", "exercise")
                    putExtra("json", gson.toJson(result.exercise))
                }
                startActivity(intent)
            }

            is APIContentResult.Workouts -> {
                val intent = Intent(this, ContentListActivity::class.java).apply {
                    putExtra("type", "workouts")
                    putExtra("json", gson.toJson(result.workouts))
                }
                startActivity(intent)
            }

            is APIContentResult.Plans -> {
                val intent = Intent(this, ContentListActivity::class.java).apply {
                    putExtra("type", "plans")
                    putExtra("json", gson.toJson(result.plans))
                }
                startActivity(intent)
            }

            is APIContentResult.Exercises -> {
                val intent = Intent(this, ContentListActivity::class.java).apply {
                    putExtra("type", "exercises")
                    putExtra("json", gson.toJson(result.exercises))
                }
                startActivity(intent)
            }

            is APIContentResult.Error -> {
                Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


enum class FilterType { NONE, CATEGORY, BODY_PARTS }
enum class SearchType { BY_ID, BY_TITLE }

@Composable
fun ContentSelectionScreen(
    selectedContent: ContentType,
    onContentChange: (ContentType) -> Unit,
    selectedFilter: FilterType,
    onFilterChange: (FilterType) -> Unit,
    selectedSearchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    selectedBodyParts: List<BodyPart>,
    onBodyPartToggle: (BodyPart) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    isLoading: Boolean,
    onFetchContent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text("Select Content Type:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedButtonRow(
            options = listOf(ContentType.WORKOUT, ContentType.PLAN, ContentType.EXERCISE),
            selected = selectedContent,
            onSelected = onContentChange,
            labelMapper = {
                it.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Filter By:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedButtonRow(
            options = listOf(FilterType.NONE, FilterType.CATEGORY, FilterType.BODY_PARTS),
            selected = selectedFilter,
            onSelected = onFilterChange,
            labelMapper = {
                when (it) {
                    FilterType.NONE -> "None"
                    FilterType.CATEGORY -> "Category"
                    FilterType.BODY_PARTS -> "Body Parts"
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedFilter == FilterType.NONE) {
            Text("Search By:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            SegmentedButtonRow(
                options = listOf(SearchType.BY_ID, SearchType.BY_TITLE),
                selected = selectedSearchType,
                onSelected = onSearchTypeChange,
                labelMapper = { if (it == SearchType.BY_ID) "Find By ID" else "Find By Title" }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (selectedFilter != FilterType.BODY_PARTS) {
            val label = when (selectedFilter) {
                FilterType.NONE -> "${
                    selectedContent.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }
                } ${
                    if (selectedSearchType == SearchType.BY_ID) "ID" else "Title"
                }"

                else -> "Category"
            }
            Text("Enter $label", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Cardio") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Text("Select Body Parts", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(BodyPart.entries) { part ->
                    FilterChip(
                        selected = selectedBodyParts.contains(part),
                        onClick = { onBodyPartToggle(part) },
                        label = {
                            Text(
                                part.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = LocalTextStyle.current.copy(
                                    color = if (selectedBodyParts.contains(part)) Color.White else Color.Gray
                                )
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors().copy(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFetchContent,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "View ${
                        selectedContent.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                    }",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun <T> SegmentedButtonRow(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    labelMapper: (T) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            TextButton(
                onClick = { onSelected(option) },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) Color.White else Color.Black
                )
            ) {
                Text(labelMapper(option), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
