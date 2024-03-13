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
    var message: MutableLiveData<String> = MutableLiveData("")
    var workoutData: MutableLiveData<String> = MutableLiveData("")

    fun handle(message: String) {

        try {
            val json = JSONObject(message)
            when (json.getString("type")) {
                "finished_workout" -> workoutData.postValue("\nWorkout finished, data received: ${json.getString("data")} ")
                "error_occured" -> workoutData.postValue("\nThere was an error: ${json.getString("data")} ")
                "exercise_completed" -> workoutData.postValue("\nExercise completed: ${json.getString("data")} ")
                "exitApp" -> {

                    Log.e("TAG_viewmodel", "handle: " )
                    showWebView.postValue(State.ERROR.name)
                    workoutData.postValue("\nUser closed workout window ")
                }
                else -> { }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            println("Could not parse JSON message from WebView.")
        }
    }


}