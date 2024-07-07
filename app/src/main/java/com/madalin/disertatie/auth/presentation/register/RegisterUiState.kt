package com.madalin.disertatie.auth.presentation.register

import com.madalin.disertatie.core.presentation.components.StatusDialogData
import com.madalin.disertatie.core.presentation.components.StatusDialogType
import com.madalin.disertatie.core.presentation.util.UiText

data class RegisterUiState(
    val isRegisterOperationComplete: Boolean = false,

    // field errors
    val emailError: UiText = UiText.Empty,
    val passwordError: UiText = UiText.Empty,
    val confirmPasswordError: UiText = UiText.Empty,

    // status dialog
    val isStatusDialogVisible: Boolean = false,
    val statusDialogData: StatusDialogData = StatusDialogData(StatusDialogType.Processing, UiText.Empty)
)