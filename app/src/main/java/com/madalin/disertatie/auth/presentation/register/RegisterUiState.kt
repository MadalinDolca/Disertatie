package com.madalin.disertatie.auth.presentation.register

import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.components.StatusDialogData
import com.madalin.disertatie.core.presentation.components.StatusDialogType
import com.madalin.disertatie.core.presentation.util.UiText

data class RegisterUiState(
    val isRegisterOperationComplete: Boolean = false,

    // field data
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // field errors
    val emailError: /*EmailFieldError? = null*/UiText = UiText.Empty,
    val passwordError: /*PasswordFieldError? = null*/UiText = UiText.Empty,
    val confirmPasswordError: /*ConfirmPasswordFieldError? = null*/UiText = UiText.Empty,

    // status dialog
    val isStatusDialogVisible: Boolean = false,
    val statusDialogData: StatusDialogData = StatusDialogData(StatusDialogType.Processing, UiText.Empty),

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty)
)