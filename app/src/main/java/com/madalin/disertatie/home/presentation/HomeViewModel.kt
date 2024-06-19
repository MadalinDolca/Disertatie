package com.madalin.disertatie.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce()
            }
        }
    }

    private fun GlobalState.reduce() {
        _uiState.update { currentState ->
            currentState.copy(launchedTrailId = this.launchedTrailId)
        }
    }
}