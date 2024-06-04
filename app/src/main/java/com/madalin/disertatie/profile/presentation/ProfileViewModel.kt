package com.madalin.disertatie.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
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

        //viewModelScope.launch {
        getUserTrails()
        //}
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
     * Obtains the trails made by the current user.
     */
    private fun getUserTrails() {
        firebaseContentRepository.getTrailsByUserId(_uiState.value.currentUser.id,
            onSuccess = { trailsList ->
                _uiState.update { it.copy(userTrails = trailsList) }
            },
            onFailure = {
                val message = it ?: R.string.could_not_load_trails
                showStatusBanner(StatusBannerType.Error, message)
            }
        )
    }

    /**
     * Shows a [global][GlobalDriver] status banner with this [type] and [text] message or resource ID.
     */
    private fun showStatusBanner(type: StatusBannerType, text: Any) {
        val uiText = when (text) {
            is String -> UiText.Dynamic(text)
            is Int -> UiText.Resource(text)
            else -> UiText.Empty
        }

        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(type, uiText)))
        globalDriver.handleAction(GlobalAction.ShowStatusBanner)
    }

    /**
     * Logs out the current user.
     */
    private fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}