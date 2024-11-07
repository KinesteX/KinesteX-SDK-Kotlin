# Content API Documentation

## Introduction

The KinesteX Content API allows you to fetch workout plans, workouts, and exercises from the KinesteX platform. This API provides a simple and efficient way to integrate KinesteX content into your Android applications.

---

## Usage

### Prerequisites

- **API Key**: You must have a valid API key provided by KinesteX.
- **Company Name**: Your company's name as registered with KinesteX.
- **Dependencies**: Ensure you have Kotlin Coroutines in your project and latest version of KinesteXSDK. Check latest version [here](https://jitpack.io/#KinesteX/KinesteX-SDK-Kotlin)

### Fetching Content

You can fetch different types of content by specifying the `ContentType`. The available content types are:

- `ContentType.WORKOUT`
- `ContentType.PLAN`
- `ContentType.EXERCISE`

Here's how you can fetch content:

```kotlin
btnApiRequest.setOnClickListener {
    lifecycleScope.launch {
        try {
            // Show loading state if needed
            // binding.progressBar.visibility = View.VISIBLE

            // Switch to IO dispatcher for network request
            val result = withContext(Dispatchers.IO) {
                fetchContent(
                    apiKey = apiKey,
                    companyName = company,
                    contentType = ContentType.PLAN,
                    title = "Circuit Training" // example plan
                )

                // FOR FETCHING WORKOUT
                // fetchContent(apiKey, company, ContentType.WORKOUT, title = "Fitness Lite")

                // FOR FETCHING EXERCISE
                // fetchContent(apiKey, company, ContentType.EXERCISE, title = "Squats")
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
- **Fetch Content**: The `fetchContent` function is called with the necessary parameters to fetch the desired content.
- **Handle Result**: After fetching, the result is passed to `handleAPIResult` to process the response.

### Fetch Content Function

```kotlin
private suspend fun fetchContent(
    apiKey: String,
    companyName: String,
    contentType: ContentType,
    id: String? = null,
    title: String? = null
): APIContentResult {
    return KinesteXAPI.fetchAPIContentData(
        apiKey = apiKey,
        companyName = companyName,
        contentType = contentType,
        title = title,
        id = id
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
    - `Workout`: Contains a `WorkoutModel`.
    - `Plan`: Contains a `PlanModel`.
    - `Exercise`: Contains an `ExerciseModel`.
  - **Error Case**:
    - `Error`: Contains an error message.
- **Handling Data**:
  - Use `Gson` with pretty printing to convert the result into a readable JSON format.
  - Print the data to the console or handle it as needed in your application.
- **Handling Errors**:
  - Display a toast message or handle the error appropriately.

---

## Implementation Overview

The core of the Content API lies in the `KinesteXAPI` class and related data models. Here's a brief overview:

### KinesteXAPI Class

Responsible for making network requests to the KinesteX server to fetch content data.

```kotlin
class KinesteXAPI {
    companion object {
        private const val BASE_API_URL = "https://admin.kinestex.com/api/v1/"

        suspend fun fetchAPIContentData(
            apiKey: String,
            companyName: String,
            contentType: ContentType,
            id: String? = null, // id of the plan
            title: String? = null,
            lang: String = "en"
        ): APIContentResult {
            // Implementation details...
        }

        private fun containsDisallowedCharacters(text: String): Boolean {
            // Validation logic...
        }
    }
}
```

#### Key Points

- **Endpoints**: Constructs the appropriate endpoint based on `ContentType`.
- **Headers**: Adds `x-api-key` and `x-company-name` to authenticate requests.
- **Network Call**: Uses `OkHttpClient` to perform synchronous network calls.
- **Error Handling**: Returns an `APIContentResult.Error` in case of failures.

### Data Models

Data classes representing the structure of the content:

- **WorkoutModel**
- **ExerciseModel**
- **PlanModel**

These models represent the data received from the API and are used throughout your application.

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

---

## Conclusion

The KinesteX Content API provides a straightforward way to access workout content within your Android application. By following the usage examples and understanding the importance of coroutine dispatchers, you can efficiently integrate and handle content data.

For any issues or further assistance, please contact KinesteX support at [support@kinestex.com](mailto:support@kinestex.com).

---
