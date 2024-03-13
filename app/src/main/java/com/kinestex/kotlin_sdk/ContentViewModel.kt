package com.kinestex.kotlin_sdk


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentViewModel : ViewModel() {
    val showWebView: MutableLiveData<String> = MutableLiveData(State.LOADING.name)

    fun handle(message: String) {

        try {
            val json = JSONObject(message)
            when (json.getString("type")) {
                "finished_workout" -> println("\nWorkout finished, data received: ${json.getString("data")} ")
                "error_occured" -> println("\nThere was an error: ${json.getString("data")} ")
                "exercise_completed" -> println("\nExercise completed: ${json.getString("data")} ")
                "exitApp" -> {

                    Log.e("TAG_viewmodel", "handle: " )
                    showWebView.postValue(State.ERROR.name)
                    println("\nUser closed workout window ")
                }
                "kinestex_launched" -> println("\nLaunched KinesteX: ${json.getString("data")} ")
                "workoutStarted" -> println("\nWorkout started: ${json.getString("data")} ")
                "workoutOpened" -> println("\nWorkout opened: ${json.getString("data")} ")
                "plan_unlocked" -> println("\nPlan unlocked: ${json.getString("data")} ")

                else -> { println("Type: ${json.getString("type")}, Data: ${json.getString("data")}") }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            println("Could not parse JSON message from WebView.")
        }
    }


}