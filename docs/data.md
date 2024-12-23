## **Handling Data**:

Here's an example of how you can handle the available data types for the `handleWebViewMessage`:
```kotlin
private fun handleWebViewMessage(message: WebViewMessage) {
    when (message) {
        is WebViewMessage.KinestexLaunched -> {
            println("KinesteX launched at: $message")
        }
        is WebViewMessage.FinishedWorkout -> {
            println("Finished Workout: $message")
        }
        is WebViewMessage.ErrorOccurred -> {
            println("Error Occurred: $message")
        }
        is WebViewMessage.ExerciseCompleted -> {
            println("Exercise Completed: $message")
        }
        is WebViewMessage.ExitKinestex -> {
            println("Exited KinesteX at: $message")
        }
        is WebViewMessage.WorkoutOpened -> {
            println("Workout Opened: $message")
        }
        is WebViewMessage.WorkoutStarted -> {
            println("Workout Started: $message")
        }
        is WebViewMessage.PlanUnlocked -> {
            println("Plan Unlocked: $message")
        }
        is WebViewMessage.CustomType -> {
            println("Any other message: $message")
        }
         is WebViewMessage.WorkoutOverview -> {
            println("Workout Overview: $message")
        }
        is WebViewMessage.ExerciseOverview -> {
            println("Exercise Overview: $message")
        }
        is WebViewMessage.WorkoutCompleted -> {
            println("Workout Completed: $message")
        }

        // CAMERA COMPONENT SPECIFIC
        is WebViewMessage.Reps -> {
            println("Reps: $message")
        }
        is WebViewMessage.Mistake -> {
            println("Mistake: $message")
        }
       
    }
}
``` 
## Available Data Types

| Type                    | Data Format                                                                                                 | Description                                                                                      |
|-------------------------|-------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `kinestex_launched`      | `dd mm yyyy hours:minutes:seconds`                                                                          | Logs when the KinesteX view is launched.                                                         |
| `exit_kinestex`          | `date: dd mm yyyy hours:minutes:seconds`, `time_spent: number`                                              | Logs when the user exits the KinesteX view, including total time spent since launch in seconds.  |
| `plan_unlocked`          | `title: String, date: date and time`                                                                        | Logs when a workout plan is unlocked.                                                            |
| `workout_opened`         | `title: String, date: date and time`                                                                        | Logs when a workout is opened.                                                                   |
| `workout_started`        | `title: String, date: date and time`                                                                        | Logs when a workout begins.                                                                      |
| `error_occurred`         | `data: string`                                                                                              | Logs significant errors, such as missing camera permissions.                                     |
| `exercise_completed`     | `time_spent: number`, `repeats: number`, `calories: number`, `exercise: string`, `mistakes: [string: number]`| Logs each completed exercise.                                                                    |
| `left_camera_frame`      | `number`                                                                                                    | Indicates when the user leaves the camera frame, with current `total_active_seconds`.           |
| `returned_camera_frame`  | `number`                                                                                                    | Indicates when the user returns to the camera frame, with current `total_active_seconds`.       |
| `workout_overview`       | `workout: string`, `total_time_spent: number`, `total_repeats: number`, `total_calories: number`, `percentage_completed: number`, `total_mistakes: number` | Logs a workout summary upon completion.                              |
| `exercise_overview`      | `[exercise_completed]`                                                                                      | Returns a log of all exercises and their data.                                                   |
| `workout_completed`      | `workout: string`, `date: dd mm yyyy hours:minutes:seconds`                                                 | Logs when a workout is completed and the user exits the overview.                               |
| `active_days` (Coming Soon) | `number`                                                                                                | Tracks the number of days the user has accessed KinesteX.                                         |
| `total_workouts` (Coming Soon) | `number`                                                                                            | Tracks the total number of workouts completed by the user.                                       |
| `workout_efficiency` (Coming Soon) | `number`                                                                                         | Measures workout intensity, with average efficiency set at 0.5, indicating 80% completion within the average timeframe. |
---