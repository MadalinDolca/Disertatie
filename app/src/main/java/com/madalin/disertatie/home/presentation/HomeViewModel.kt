package com.madalin.disertatie.home.presentation

import androidx.lifecycle.ViewModel
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val globalDriver: GlobalDriver,
    private val userRepository: FirebaseUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun showDialog() {
        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(StatusBannerType.Info, UiText.Dynamic("hohoho"))))
        globalDriver.handleAction(GlobalAction.ShowStatusBanner)
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}