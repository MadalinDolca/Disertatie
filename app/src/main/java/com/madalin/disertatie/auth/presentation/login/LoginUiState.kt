package com.madalin.disertatie.auth.presentation.login

import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText

data class LoginUiState(
    val isLoginOperationComplete: Boolean = false,

    // field errors
    val emailError: UiText = UiText.Empty,
    val passwordError: UiText = UiText.Empty,

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty)
)