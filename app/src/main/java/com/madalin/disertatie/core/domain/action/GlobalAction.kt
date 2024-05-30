package com.madalin.disertatie.core.domain.action

import com.madalin.disertatie.core.presentation.components.StatusBannerData

sealed class GlobalAction {
    data class SetUserLoginStatus(val isLoggedIn: Boolean) : GlobalAction()
    data object ListenForUserData : GlobalAction()
    data class SetStatusBannerData(val data: StatusBannerData) : GlobalAction()
    data object ShowStatusBanner : GlobalAction()
    data object HideStatusBanner : GlobalAction()
}