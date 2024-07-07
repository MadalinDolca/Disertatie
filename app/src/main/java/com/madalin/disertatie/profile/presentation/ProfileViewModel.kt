package com.madalin.disertatie.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.result.TrailsListResult
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.profile.presentation.action.ProfileAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val globalDriver: GlobalDriver,
    private val firebaseContentRepository: FirebaseContentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // collect global state
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce()
            }
        }

        getUserTrails()
    }

    private fun GlobalState.reduce() {
        _uiState.update { currentState ->
            currentState.copy(
                currentUser = this.currentUser
            )
        }
    }

    /**
     * Handles the given [ProfileAction] by calling the appropriate handle method.
     */
    fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.DoLogout -> logout()
        }
    }

    /**
     * Obtains the trails made by the current user and listens for updates.
     */
    private fun getUserTrails() {
        viewModelScope.launch {
            val result = launch {
                firebaseContentRepository
                    .observeTrailsByUserId(_uiState.value.currentUser.id)
                    .collect { result ->
                        when (result) {
                            is TrailsListResult.Success -> _uiState.update { it.copy(userTrails = result.trails) }
                            is TrailsListResult.Error -> globalDriver.onAction(
                                GlobalAction.ShowStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_load_trails)
                            )
                        }
                    }
            }
            result.join() // keeps the coroutine running to listen for updates
        }
    }

    /**
     * Logs out the current user.
     */
    private fun logout() {
        globalDriver.onAction(GlobalAction.SetUserLoginStatus(false))
    }
}