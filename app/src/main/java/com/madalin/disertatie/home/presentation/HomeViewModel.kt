package com.madalin.disertatie.home.presentation

import androidx.compose.runtime.mutableStateListOf
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeLast()
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted) {
            visiblePermissionDialogQueue.add(0, permission)
        }
    }

    fun showDialog() {
        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(StatusBannerType.Info, UiText.Dynamic("hohoho"))))
        globalDriver.handleAction(GlobalAction.ShowStatusBanner)
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}