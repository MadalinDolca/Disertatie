package com.madalin.disertatie.core.presentation

import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText

data class MainActivityUiState(
    // user login status
    val isUserLoggedIn: Boolean = false,

    // splash screen
    val isSplashScreenVisible: Boolean = true,

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty)
)