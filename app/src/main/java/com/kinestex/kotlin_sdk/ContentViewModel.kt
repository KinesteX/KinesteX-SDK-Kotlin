package com.kinestex.kotlin_sdk

/*
 * Created by shagi on 22.02.2024 22:27
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kinestex.kotlin_sdk.data.IntegrationOptionType
import com.kinestex.kotlin_sdk.data.IntegrationOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ContentViewModel : ViewModel() {
    val showWebView: MutableStateFlow<WebViewState> = MutableStateFlow(WebViewState.LOADING)

    val reps: MutableStateFlow<Int> = MutableStateFlow(0)
    val mistake: MutableStateFlow<String> = MutableStateFlow("")

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var selectedOptionPosition: MutableStateFlow<Int> = MutableStateFlow(0)

    var selectedSubOption: Int = 0
    var integrateOptions: List<IntegrationOption> = generateOptions()

    private fun generateOptions(): List<IntegrationOption> {
        return IntegrationOptionType.entries.map { optionType ->
            IntegrationOption(
                title = optionType.title,
                optionType = optionType.category,
                subOption = optionType.subOptions?.toMutableList()
            )
        }
    }

    fun setOption(i: Int) {
        selectedOptionPosition.value = i
    }

    fun setMistake(it: String) {
        viewModelScope.launch {
            mistake.emit(it)
        }
    }

    fun setReps(it: Int) {
        viewModelScope.launch {
            reps.emit(it)
        }
    }
}