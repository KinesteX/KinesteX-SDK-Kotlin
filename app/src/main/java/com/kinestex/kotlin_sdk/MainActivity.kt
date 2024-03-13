package com.kinestex.kotlin_sdk

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.kinestex.kinestexsdkkotlin.KinesteXWebView
import com.kinestex.kinestexsdkkotlin.models.MessageCallback
import com.kinestex.kinestexsdkkotlin.models.PlanCategory
import com.kinestex.kinestexsdkkotlin.models.WorkoutCategory

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ContentViewModel
    private var kinesteXWebView: KinesteXWebView? = null
    private lateinit var rootView: ConstraintLayout

    val callback = object : MessageCallback {
        override fun onMessageReceived(message: String) {
            viewModel.handle(message)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView = findViewById(R.id.main)

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        viewModel.showWebView.observe(this) {
            if (it.equals(State.ERROR.name)) {
                finish()
            }
        }

        kinesteXWebView = KinesteXWebView.createWebView(
            this,
            "YOUR API KEY",
            "YOUR COMPANY NAME",
            "YOUR USER ID",
            PlanCategory.Cardio,
            WorkoutCategory.Fitness,
            callback
        )

        if (kinesteXWebView == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        } else {
            rootView.addView(kinesteXWebView!!.webView)
        }

    }


}
