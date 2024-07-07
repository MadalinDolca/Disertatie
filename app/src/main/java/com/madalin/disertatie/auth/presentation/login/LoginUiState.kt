package com.madalin.disertatie.auth.presentation.login

import com.madalin.disertatie.core.presentation.util.UiText

data class LoginUiState(
    val isLoginOperationComplete: Boolean = false,

    // field errors
    val emailError: UiText = UiText.Empty,
    val passwordError: UiText = UiText.Empty,
)