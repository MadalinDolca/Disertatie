package com.madalin.disertatie.auth.presentation.actions

sealed class PasswordResetAction {
    data class ResetPassword(val email: String) : PasswordResetAction()
    data object HideStatusBanner : PasswordResetAction()
}