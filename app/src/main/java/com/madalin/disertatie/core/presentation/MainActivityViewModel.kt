package com.madalin.disertatie.core.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.core.domain.actions.GlobalAction
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
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce()
            }
        }

        globalDriver.handleAction(GlobalAction.ListenForUserData)
    }

    private fun GlobalState.reduce() {
        _state.update { currentState ->
            currentState.copy(
                isUserLoggedIn = this.isUserLoggedIn,
                isStatusBannerVisible = this.isStatusBannerVisible,
                statusBannerData = this.statusBannerData
            )
        }
        Log.d("MainActivityViewModel", "reduced ${_state.value}")
    }

    fun toggleStatusBannerVisibility(isVisible: Boolean) {
        if (isVisible) globalDriver.handleAction(GlobalAction.ShowStatusBanner)
        else globalDriver.handleAction(GlobalAction.HideStatusBanner)
    }
}