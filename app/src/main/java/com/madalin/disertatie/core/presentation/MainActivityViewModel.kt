package com.madalin.disertatie.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.core.domain.action.GlobalAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val globalDriver: GlobalDriver,
) : ViewModel() {
    private val _state = MutableStateFlow(MainActivityUiState())
    val state = _state.asStateFlow()

    init {
        // collect global state
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce()
            }
        }

        // splash screen delay
        viewModelScope.launch {
            delay(1000L)
            _state.update { it.copy(isSplashScreenVisible = false) }
        }

        globalDriver.onAction(GlobalAction.ListenForUserData)
    }

    private fun GlobalState.reduce() {
        _state.update { currentState ->
            currentState.copy(
                isUserLoggedIn = this.isUserLoggedIn,
                isStatusBannerVisible = this.isStatusBannerVisible,
                statusBannerData = this.statusBannerData
            )
        }
    }

    fun hideStatusBanner() {
        globalDriver.onAction(GlobalAction.HideStatusBanner)
    }
}