package com.madalin.disertatie.auth.presentation.password_reset

import com.madalin.disertatie.core.presentation.util.UiText

data class PasswordResetUiState(
    // field error
    val emailError: UiText = UiText.Empty
)