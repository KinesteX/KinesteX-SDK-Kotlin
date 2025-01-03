package com.kinestex.kotlin_sdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.kinestex.kinestexsdkkotlin.APIContentResult
import com.kinestex.kinestexsdkkotlin.BodyPart
import com.kinestex.kinestexsdkkotlin.ContentType
import com.kinestex.kinestexsdkkotlin.GenericWebView
import com.kinestex.kinestexsdkkotlin.KinesteXAPI
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.PermissionHandler
import com.kinestex.kinestexsdkkotlin.PlanCategory
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import com.kinestex.kotlin_sdk.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), PermissionHandler {
    private var tvMistake: TextView? = null
    private var tvReps: TextView? = null

    private lateinit var viewModel: ContentViewModel
    private lateinit var binding: ActivityMainBinding
    private val iconSubOptions = mutableListOf<ImageView>()
    private var webView: GenericWebView? = null

    private val apiKey = "your_api_key" // store this key securely
    private val company = "your_company_name"
    private val userId = "userId"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
       webView?.handlePermissionResult(isGranted)
    }
    override fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        initUiListeners()

        observe()
    }


    private fun showHowToVideo() {
        val howToView = KinesteXSDK.createHowToView(
            context = this,
            onVideoEnd = {
                didEnd ->
                if (didEnd) {
                    Toast.makeText(this, "How to view ended", Toast.LENGTH_SHORT).show()
                }
            },
            videoURL = "https://cdn.kinestex.com/SDK%2Fhow-to-video%2Foutput_compressed.mp4?alt=media&token=9a3c0ed8-c86b-4553-86dd-a96f23e55f74",
            onCloseClick = {
                binding.layVideo.removeAllViews()
            }
        )

        // Add the howToView to your layout
        binding.layVideo.addView(howToView)
    }


    private fun initUiListeners() {
        binding.apply {
            toggleOptions.setOnClickListener { handleDropDown(iconDropdown, collapsableContent) }
            toggleOptionsCategory.setOnClickListener {
                handleDropDown(
                    iconDropdownCategory, collapsableContentCategory
                )
            }

            next.setOnClickListener { handleNextButton() }

            completeUx.setOnClickListener { handleOptionSelection(0, iconRadioCompleteUx) }
            workoutPlan.setOnClickListener { handleOptionSelection(1, iconRadioWorkoutPlan) }
            workout.setOnClickListener { handleOptionSelection(2, iconRadioWorkout) }
            challenge.setOnClickListener { handleOptionSelection(3, iconRadioChallenge) }
            leaderboard.setOnClickListener { handleOptionSelection(6, iconLeaderboard) }
            experience.setOnClickListener { handleOptionSelection(5, iconRadioExperience) }
            camera.setOnClickListener { handleOptionSelection(4, iconRadioCamera) }
            btnJumpingJack.setOnClickListener {
                KinesteXSDK.updateCurrentExercise("Jumping Jack")
            }

            btnTestVideo.setOnClickListener {
                showHowToVideo()
            }
            btnApiRequest.setOnClickListener {
                lifecycleScope.launch {
                    try {
                        // Show loading state if needed
                        // binding.progressBar.visibility = View.VISIBLE
                        // Switch to IO dispatcher for network request

                        val result = withContext(Dispatchers.IO) {
                           // FOR FETCHING PLANS LIST
                            fetchContent(apiKey, company, contentType = ContentType.PLAN, category = "Cardio", limit = 5)

                            // FOR FETCHING WORKOUTS LIST (with category and body parts)
                            //fetchContent(apiKey, company, contentType = ContentType.WORKOUT, category = "Fitness", limit = 5)
                            //fetchContent(apiKey, company, contentType = ContentType.WORKOUT, bodyParts = listOf(BodyPart.ABS), limit = 5)

                            // FOR FETCHING EXERCISES LIST (with body parts)
                            // fetchContent(apiKey, company, contentType = ContentType.EXERCISE, bodyParts = listOf(BodyPart.ABS), limit = 5)

                            // FOR FETCHING PLAN
                            // fetchContent(apiKey, company, contentType = ContentType.PLAN, title = "Circuit Training")

                            // FOR FETCHING WORKOUT
                            // fetchContent(apiKey, company, contentType = ContentType.WORKOUT, title = "Fitness Lite")

                            // FOR FETCHING EXERCISE
                            // fetchContent(apiKey, company, contentType = ContentType.EXERCISE, title = "Squats")
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

        }
    }

        private suspend fun fetchContent(apiKey: String, companyName: String, contentType: ContentType,
                                         id: String? = null, title: String? = null,
                                         bodyParts: List<BodyPart>? = null, lastDocId: String? = null,
                                         category: String? = null, limit: Int? = null): APIContentResult {
            return KinesteXAPI.fetchAPIContentData(
                apiKey = apiKey,
                companyName = companyName,
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
            when (result) {
                is APIContentResult.Workout -> {
                    // Handle successful workout data
                    val workout = result.workout
                    // Create a Gson instance with pretty printing
                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(workout)

                    // Print the formatted JSON string
                    println("Workout Data:\n$prettyJson")
                }
                is APIContentResult.Plan -> {
                    // Handle successful workout data
                    val workout = result.plan
                    // Create a Gson instance with pretty printing
                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(workout)

                    // Print the formatted JSON string
                    println("Plan Data:\n$prettyJson")
                }
                is APIContentResult.Exercise -> {
                    // Handle successful workout data
                    val workout = result.exercise
                    // Create a Gson instance with pretty printing
                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(workout)

                    // Print the formatted JSON string
                    println("Exercise Data:\n$prettyJson")
                }
                is APIContentResult.Workouts -> {

                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(result.workouts)

                    // Print the formatted JSON string
                    println("Workouts Data:\n$prettyJson")
                }
                is APIContentResult.Plans -> {

                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(result.plans)

                    // Print the formatted JSON string
                    println("Plans Data:\n$prettyJson")
                }

                is APIContentResult.Exercises -> {

                    val gson = GsonBuilder().setPrettyPrinting().create()

                    // Convert the workout object to a pretty JSON string
                    val prettyJson = gson.toJson(result.exercises)

                    // Print the formatted JSON string
                    println("Exercises Data:\n$prettyJson")
                }
                is APIContentResult.Error -> {
                    println("ERROR: ${result.message}")
                    // Handle error
                    Toast.makeText(
                        this,
                        "Error: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    // Handle unexpected result type
                    Toast.makeText(
                        this,
                        "Unexpected result type",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun handleNextButton() {
        createWebView()?.let { view ->
            viewModel.showWebView.value = WebViewState.SUCCESS

            if (viewModel.selectedOptionPosition.value == 4) {
                binding.layoutWebView.addView(view)
            }
        }
    }

    private fun handleOptionSelection(position: Int, icon: ImageView) {
        uncheckOldPosition()
        viewModel.setOption(position)
        setChecked(icon)
    }

    private fun setChecked(icon: ImageView) {
        icon.setImageResource(R.drawable.radio_active)
    }

    private fun uncheckOldPosition() {
        val currentPosition = viewModel.selectedOptionPosition.value

        val icons = listOf(
            binding.iconRadioCompleteUx,
            binding.iconRadioWorkoutPlan,
            binding.iconRadioWorkout,
            binding.iconRadioChallenge,
            binding.iconRadioCamera,
            binding.iconRadioExperience,
            binding.iconLeaderboard,
        )

        icons[currentPosition].setImageResource(R.drawable.radio_unchecked)
    }

    private fun handleDropDown(icon: View, collapsableContent: LinearLayout) {
        val rotation = if (icon.rotation == 0f) 180f else 0f
        icon.animate().rotation(rotation).duration = 200
        if (rotation == 180f) {
            AnimationUtils.expand(collapsableContent, 200)
        } else {
            AnimationUtils.collapse(collapsableContent, 200)
        }
    }

    private fun setFullScreen() {

        window.apply {
            // Make status bar transparent
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT

            // Make navigation bar transparent
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            navigationBarColor = Color.TRANSPARENT
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun createWebView(): View? {
        val subOption =
            viewModel.integrateOptions[viewModel.selectedOptionPosition.value].subOption?.get(
                viewModel.selectedSubOption
            )
        val view = when (viewModel.selectedOptionPosition.value) {
            0 -> {
                val data = mutableMapOf<String, Any>()

               // data["style"] = "light" // passing forcefully the style theme
                // data["isHideHeaderMain"] = false // should display header in main screen

                webView = KinesteXSDK.createMainView(
                    this,
                    apiKey,
                    company,
                    userId,
                    getPlanCategory(subOption),
                    null,
                    customParams = data, // example of using custom parameters
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    this
                ) as GenericWebView?
                return webView
            }

            1 -> {

                val data = mutableMapOf<String, Any>()
                data["style"] = "light" // passing forcefully the planCategory

                webView = KinesteXSDK.createPlanView(
                    this,
                    apiKey,
                    company,
                    userId,
                    subOption ?: "Circuit Training",
                    null,
                    data,
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    this
                ) as GenericWebView?
                return webView

            }

            2 -> {
                webView = KinesteXSDK.createWorkoutView(
                    this,
                    apiKey,
                    company,
                    userId,
                    subOption ?: "Fitness Lite",
                    null,
                    isLoading = viewModel.isLoading,
                    onMessageReceived = ::handleWebViewMessage,
                    permissionHandler = this
                ) as GenericWebView?
                return webView
            }

            3 -> {
                webView = KinesteXSDK.createChallengeView(
                    this,
                    apiKey,
                    company,
                    userId,
                    subOption ?: "",
                    100,
                    null,
                    customParams = null,
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this,
                ) as GenericWebView?
                return webView
            }

            5 -> {
                webView = KinesteXSDK.createExperiencesView(
                    this,
                    apiKey,
                    company,
                    userId,
                    subOption ?: "",
                    100,
                    null,
                    customParams = null,
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this
                ) as GenericWebView?
                return webView
            }
            6 -> {
                webView = KinesteXSDK.createLeaderboardView(
                    this,
                    apiKey,
                    company,
                    userId,
                    "Squats",
                    "", // optional username
                    customParams = null,
                    viewModel.isLoading,
                    ::handleWebViewMessage,
                    permissionHandler = this
                ) as GenericWebView?
                return webView
            }

            4 -> createCameraComponentView(this)
            else -> null
        }
        return view?.let { setLayoutParamsFullScreen(it) }
    }

    private fun getPlanCategory(subOption: String?): PlanCategory {
        return when (subOption?.lowercase()) {
            "cardio" -> PlanCategory.Cardio
            "weightmanagement" -> PlanCategory.WeightManagement
            "strength" -> PlanCategory.Strength
            "rehabilitation" -> PlanCategory.Rehabilitation
            else -> PlanCategory.Custom(subOption ?: "")
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.showWebView.collect { state ->
                when (state) {
                    WebViewState.LOADING -> { /* show loading */
                    }

                    WebViewState.ERROR -> binding.layoutWebView.removeAllViews()
                        .also { binding.layoutWebView.visibility = View.GONE }

                    WebViewState.SUCCESS -> {
                        binding.layoutWebView.visibility = View.VISIBLE

                        if (viewModel.selectedOptionPosition.value == 4) return@collect
                        webView?.let { binding.layoutWebView.addView(setLayoutParamsFullScreen(it)) }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedOptionPosition.collect { position ->
                binding.next.text = "View ${viewModel.integrateOptions[position].title}"
                createSubOption(position)
            }
        }

        lifecycleScope.launch {
            viewModel.mistake.collect { mistake ->
                tvMistake?.let {
                    updateTextView(
                        it, "MISTAKE: $mistake"
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.reps.collect { reps -> tvReps?.let { updateTextView(it, "REPS: $reps") } }
        }
    }

    private fun createSubOption(optionPosition: Int) {
        val subOptions = viewModel.integrateOptions[optionPosition].subOption

        binding.apply {
            if (subOptions.isNullOrEmpty()) {
                layoutCategory.visibility = View.GONE
            } else {
                layoutCategory.visibility = View.VISIBLE
                collapsableContentCategory.removeAllViews()
                iconSubOptions.clear()
                viewModel.selectedSubOption = 0

                subOptions.forEachIndexed { index, title ->
                    createOptionView(this@MainActivity, title, index).also { optionView ->
                        optionView.setOnClickListener { handleSubOptionSelection(index) }
                        collapsableContentCategory.addView(optionView)
                    }
                }

                collapsableContentCategory.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    private fun handleSubOptionSelection(index: Int) {
        val oldPosition = viewModel.selectedSubOption
        iconSubOptions[oldPosition].setImageResource(R.drawable.radio_unchecked)
        viewModel.selectedSubOption = index
        iconSubOptions[index].setImageResource(R.drawable.radio_active)
    }

    private fun createCameraComponentView(context: Context): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).apply { gravity = Gravity.CENTER_VERTICAL }
            setPadding(16, 16, 16, 16)
        }

        tvReps = createTextView(context, "REPS: 0")
        tvMistake = createTextView(context, "MISTAKE: --", android.R.color.holo_red_dark)

        container.apply {
            addView(tvReps)
            addView(tvMistake)
        }

        webView = KinesteXSDK.createCameraComponent(
            context = context,
            apiKey = apiKey,
            companyName = company,
            userId = userId,
            currentExercise = "Squats",
            exercises = listOf("Squats", "Jumping Jack"),
            user = null,
            isLoading = viewModel.isLoading,
            onMessageReceived = ::handleWebViewMessage,
            permissionHandler = this
        ) as GenericWebView?

        webView?.let { container.addView(setLayoutParamsFullScreen(it)) }

        return container
    }

    private fun handleWebViewMessage(message: WebViewMessage) {
        when (message) {
            is WebViewMessage.ExitKinestex -> lifecycleScope.launch {
                viewModel.showWebView.emit(
                    WebViewState.ERROR
                )
            }

            is WebViewMessage.Reps -> {
                (message.data["value"] as? Int)?.let { viewModel.setReps(it) }
            }

            is WebViewMessage.Mistake -> {
                (message.data["value"] as? String)?.let {
                    viewModel.setMistake(
                        it
                    )
                }
            }

            else -> {
                Log.d("Message received", message.toString())
            }
        }
    }

    private fun setLayoutParamsFullScreen(view: View): View {
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        return view
    }

    private fun createTextView(
        context: Context, text: String, textColorResId: Int = android.R.color.white
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(context.resources.getColor(textColorResId, context.theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 8
            }
        }
    }

    private fun createOptionView(
        context: Context,
        title: String,
        index: Int,
    ): LinearLayout {
        val linearLayout = LinearLayout(context)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = params
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(
            dpToPx(context, 10), dpToPx(context, 7), dpToPx(context, 10), dpToPx(context, 7)
        )

        // Create TextView
        val textView = TextView(context)
        val textParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        )
        textView.layoutParams = textParams
        textView.text = title
        textView.setTextColor(Color.WHITE)

        // Create ImageView
        val imageView = ImageView(context)

        val imageParams = LinearLayout.LayoutParams(
            dpToPx(context, 20), dpToPx(context, 20)
        )

        imageView.layoutParams = imageParams
        imageView.setImageResource(if (index == 0) R.drawable.radio_active else R.drawable.radio_unchecked)

        // Add views to LinearLayout
        iconSubOptions.add(imageView)

        linearLayout.addView(textView)
        linearLayout.addView(imageView)

        return linearLayout
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }


    private fun updateTextView(textView: TextView, text: String) {
        textView.text = text
    }

    override fun onBackPressed() {

        webView?.let {
            if (it.canGoBack()) it.goBack()
            else {
                if (viewModel.showWebView.value == WebViewState.ERROR) {
                    super.onBackPressed()
                } else {
                    lifecycleScope.launch {
                        viewModel.showWebView.emit(WebViewState.ERROR)
                    }

                }
            }
            return
        }
        if (viewModel.showWebView.value == WebViewState.ERROR) {
            super.onBackPressed()
        } else {
            lifecycleScope.launch {
                viewModel.showWebView.emit(WebViewState.ERROR)
            }
        }


    }

}
