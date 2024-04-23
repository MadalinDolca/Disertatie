package com.madalin.disertatie.auth.presentation.password_reset

import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText

data class PasswordResetUiState(
    // field data
    val email: String = "",

    // field error
    val emailError: UiText = UiText.Empty,

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty)
)