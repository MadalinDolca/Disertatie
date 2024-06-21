package com.madalin.disertatie.core.domain.action

import android.location.Location
import com.madalin.disertatie.core.presentation.components.StatusBannerData

sealed class GlobalAction {
    data class SetUserLoginStatus(val isLoggedIn: Boolean) : GlobalAction()
    data class SetUserLocation(val location: Location) : GlobalAction()
    data object ListenForUserData : GlobalAction()
    data class SetStatusBannerData(val data: StatusBannerData) : GlobalAction()
    data object ShowStatusBanner : GlobalAction()
    data object HideStatusBanner : GlobalAction()
    data class SetLaunchedTrailId(val id: String) : GlobalAction()
    data object DeleteLaunchedTrailId : GlobalAction()
}