# Content API Documentation

## Introduction

The KinesteX Content API allows you to fetch workout plans, workouts, and exercises from the KinesteX platform. This API provides a simple and efficient way to integrate KinesteX content into your Android applications.

---

## Usage

### Prerequisites

- **API Key**: You must have a valid API key provided by KinesteX.
- **Company Name**: Your company's name as registered with KinesteX.
- **Dependencies**: Ensure you have Kotlin Coroutines in your project and the latest version of KinesteXSDK. Check the latest version [here](https://jitpack.io/#KinesteX/KinesteX-SDK-Kotlin).

### Fetching Content

You can fetch different types of content by specifying the `ContentType`. The available content types are:

- `ContentType.WORKOUT`
- `ContentType.PLAN`
- `ContentType.EXERCISE`

Additionally, you can fetch lists of content by providing optional parameters such as `category`, `bodyParts`, `limit`, and `lastDocId` for pagination.

Here's how you can fetch content:

```kotlin
btnApiRequest.setOnClickListener {
    lifecycleScope.launch {
        try {
            // Show loading state if needed
            // binding.progressBar.visibility = View.VISIBLE

            // Switch to IO dispatcher for network request
            val result = withContext(Dispatchers.IO) {
                // FOR FETCHING PLANS LIST
                fetchContent(
                    apiKey = apiKey,
                    companyName = company,
                    contentType = ContentType.PLAN,
                    category = "Cardio",
                    limit = 5
                )

                // FOR FETCHING WORKOUTS LIST (with category and body parts)
                // fetchContent(
                //     apiKey = apiKey,
                //     companyName = company,
                //     contentType = ContentType.WORKOUT,
                //     category = "Fitness",
                //     bodyParts = listOf(BodyPart.ABS),
                //     limit = 5
                // )

                // FOR FETCHING EXERCISES LIST (with body parts)
                // fetchContent(
                //     apiKey = apiKey,
                //     companyName = company,
                //     contentType = ContentType.EXERCISE,
                //     bodyParts = listOf(BodyPart.ABS),
                //     limit = 5
                // )

                // FOR FETCHING a Specific Plan
                // fetchContent(
                //     apiKey = apiKey,
                //     companyName = company,
                //     contentType = ContentType.PLAN,
                //     title = "Circuit Training"
                // )

                // FOR FETCHING a Specific Workout
                // fetchContent(
                //     apiKey = apiKey,
                //     companyName = company,
                //     contentType = ContentType.WORKOUT,
                //     title = "Fitness Lite"
                // )

                // FOR FETCHING a Specific Exercise
                // fetchContent(
                //     apiKey = apiKey,
                //     companyName = company,
                //     contentType = ContentType.EXERCISE,
                //     title = "Squats"
                // )
            }

            // Handle the result on the main thread
            handleAPIResult(result)
        } catch (e: Exception) {
            // Handle any errors
            Toast.makeText(
                this@MainActivity,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            // Hide loading state if needed
            // binding.progressBar.visibility = View.GONE
        }
    }
}
```

#### Explanation

- **Button Click Listener**: When the button is clicked, a coroutine is launched in the lifecycle scope.
- **Switch to IO Dispatcher**: The `withContext(Dispatchers.IO)` block ensures that the network request is performed on an IO thread, preventing UI blocking.
- **Fetch Content**: The `fetchContent` function is called with the necessary parameters to fetch the desired content. You can uncomment the relevant lines to fetch lists or specific items based on your needs.
- **Handle Result**: After fetching, the result is passed to `handleAPIResult` to process the response.

### Fetch Content Function

```kotlin
private suspend fun fetchContent(
    apiKey: String,
    companyName: String,
    contentType: ContentType,
    id: String? = null,
    title: String? = null,
    lang: String = "en",
    category: String? = null,
    lastDocId: String? = null,
    limit: Int? = null,
    bodyParts: List<BodyPart>? = null
): APIContentResult {
    return KinesteXAPI.fetchAPIContentData(
        apiKey = apiKey,
        companyName = companyName,
        contentType = contentType,
        id = id,
        title = title,
        lang = lang,
        category = category,
        lastDocId = lastDocId,
        limit = limit,
        bodyParts = bodyParts
    )
}
```

#### Explanation

- **Parameters**:
  - `apiKey`: Your KinesteX API key.
  - `companyName`: Your company name registered with KinesteX.
  - `contentType`: The type of content to fetch (`WORKOUT`, `PLAN`, `EXERCISE`).
  - `id` *(optional)*: Specific ID of the content.
  - `title` *(optional)*: Title of the content to search for.
  - `category` *(optional)*: Filter content by category.
  - `lastDocId` *(optional)*: Identifier for pagination to fetch the next set of results.
  - `limit` *(optional)*: Limit the number of results returned.
  - `bodyParts` *(optional)*: Filter workouts or exercises by targeted body parts using the `BodyPart` enum.
- **Return Value**: An `APIContentResult` object containing the fetched data or an error message.

### Handling the API Result

```kotlin
private fun handleAPIResult(result: APIContentResult) {
    when (result) {
        is APIContentResult.Workout -> {
            val workout = result.workout
            val gson = GsonBuilder().setPrettyPrinting().create()
            val prettyJson = gson.toJson(workout)
            println("Workout Data:\n$prettyJson")
        }
        is APIContentResult.Plan -> {
            val plan = result.plan
            val gson = GsonBuilder().setPrettyPrinting().create()
            val prettyJson = gson.toJson(plan)
            println("Plan Data:\n$prettyJson")
        }
        is APIContentResult.Exercise -> {
            val exercise = result.exercise
            val gson = GsonBuilder().setPrettyPrinting().create()
            val prettyJson = gson.toJson(exercise)
            println("Exercise Data:\n$prettyJson")
        }
        is APIContentResult.Workouts -> {
            val workouts = result.workouts.workouts
            workouts.forEach { workout ->
                println("Workout Title: ${workout.title}")
                println("Body Parts: ${workout.body_parts.joinToString { it.value }}")
                println("LastDocId: ${result.workouts.lastDocId}")
            }
        }
        is APIContentResult.Plans -> {
            val plans = result.plans.plans
            plans.forEach { plan ->
                println("Plan Title: ${plan.title}")
                println("Categories: ${plan.category.description}")
                println("LastDocId: ${result.plans.lastDocId}")
            }
        }
        is APIContentResult.Exercises -> {
            val exercises = result.exercises.exercises
            exercises.forEach { exercise ->
                println("Exercise Title: ${exercise.title}")
                println("Body Parts: ${exercise.body_parts.joinToString { it.value }}")
                println("LastDocId: ${result.exercises.lastDocId}")
            }
        }
        is APIContentResult.Error -> {
            Toast.makeText(
                this,
                "Error: ${result.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
```

#### Explanation

- **APIContentResult**: A sealed class representing the result of the API request.
  - **Success Cases**:
    - `Workout`: Contains a single `WorkoutModel`.
    - `Plan`: Contains a single `PlanModel`.
    - `Exercise`: Contains a single `ExerciseModel`.
    - `Workouts`: Contains a list of `WorkoutModel` along with `lastDocId` for pagination.
    - `Plans`: Contains a list of `PlanModel` along with `lastDocId` for pagination.
    - `Exercises`: Contains a list of `ExerciseModel` along with `lastDocId` for pagination.
  - **Error Case**:
    - `Error`: Contains an error message.
- **Handling Data**:
  - Use `Gson` with pretty printing to convert single item results into a readable JSON format.
  - Iterate through lists (`Workouts`, `Plans`, `Exercises`) and handle each item as needed.
  - **Pagination**: After handling the current set of results, use the provided `lastDocId` to fetch the next set of data.
- **Handling Errors**:
  - Display a toast message or handle the error appropriately.

### Pagination with `lastDocId`

To implement pagination, utilize the `lastDocId` provided in the response of your initial request. This ID allows you to fetch the next set of results in subsequent API calls.

#### Example: Fetching the Next Page of Workouts

```kotlin
// Initial Fetch
val initialResult = fetchContent(
    apiKey = apiKey,
    companyName = company,
    contentType = ContentType.WORKOUT,
    category = "Fitness",
    limit = 5
)

// Handle Initial Result
handleAPIResult(initialResult)

// Assume you have obtained lastDocId from the initialResult
val lastDocId = when (initialResult) {
    is APIContentResult.Workouts -> initialResult.workouts.lastDocId
    else -> null
}

// Fetch Next Page Using lastDocId
if (lastDocId != null) {
    val nextPageResult = fetchContent(
        apiKey = apiKey,
        companyName = company,
        contentType = ContentType.WORKOUT,
        category = "Fitness",
        limit = 5,
        lastDocId = lastDocId
    )
    
    // Handle Next Page Result
    handleAPIResult(nextPageResult)
}
```

#### Explanation

1. **Initial Fetch**: Fetch the first set of workouts with a specified `limit`.
2. **Handle Initial Result**: Process and display the fetched workouts.
3. **Retrieve `lastDocId`**: Extract the `lastDocId` from the initial response to use for the next request.
4. **Fetch Next Page**: Use the retrieved `lastDocId` to fetch the subsequent set of workouts.
5. **Handle Next Page Result**: Process and display the next set of workouts.

---

#### Key Points

- **Endpoints**: Constructs the appropriate endpoint based on `ContentType`.
- **Headers**: Adds `x-api-key` and `x-company-name` to authenticate requests.
- **Network Call**: Uses `OkHttpClient` to perform synchronous network calls.
- **Error Handling**: Returns an `APIContentResult.Error` in case of failures.
- **BodyPart Filtering**: Supports filtering by `BodyPart` enum to fetch targeted content lists.
- **Pagination**: Utilizes `lastDocId` to implement pagination, allowing you to fetch subsequent pages of content.

### Data Models

Data classes representing the structure of the content:

- **WorkoutModel**
- **ExerciseModel**
- **PlanModel**

These models represent the data received from the API and are used throughout your application. They now include `body_parts` as a list of `BodyPart` enums to ensure type safety and consistency.

---

## Notes on Coroutine Dispatchers

### Why Use `withContext(Dispatchers.IO)`?

- **Non-blocking I/O**: `Dispatchers.IO` is optimized for offloading blocking IO tasks to a shared pool of threads.
- **Prevent UI Freezing**: Performing network operations on the main thread can cause the UI to freeze, leading to a poor user experience.
- **Best Practices**: It's a recommended practice to perform network requests on a background thread to keep the app responsive.

### How It Works

```kotlin
val result = withContext(Dispatchers.IO) {
    // Network request or any IO-intensive task
}
```

- **Switching Context**: `withContext` suspends the current coroutine without blocking the thread and resumes it in the specified dispatcher (`Dispatchers.IO`).
- **Coroutine Suspension**: The coroutine will suspend until the network call is complete, and then resume on the main thread (since `lifecycleScope` is tied to the main thread).

---

## Additional Information

- **Error Handling**: Always handle possible exceptions, especially when dealing with network requests.
- **Thread Safety**: UI updates must occur on the main thread. Ensure that after fetching data on `Dispatchers.IO`, any UI operations are performed on the main thread.
- **Asynchronous Programming**: Utilizing coroutines and proper dispatchers helps in writing asynchronous code that is easy to read and maintain.
- **BodyPart Enum**: Utilize the `BodyPart` enum to specify targeted muscle groups when fetching workouts or exercises, ensuring consistency and type safety.
- **Pagination**: Use the `lastDocId` from your API responses to fetch subsequent pages of content, enabling smooth and efficient data loading.

---

## Conclusion

The KinesteX Content API provides a straightforward way to access workout content within your Android application. By following the usage examples and understanding the importance of coroutine dispatchers and pagination, you can efficiently integrate and handle content data.

With the added capability to fetch lists of workouts, plans, and exercises with filters like `category`, `bodyParts`, `limit`, and `lastDocId`, you have greater flexibility in customizing the content retrieval to suit your application's needs.

For any issues or further assistance, please contact KinesteX support at [support@kinestex.com](mailto:support@kinestex.com).