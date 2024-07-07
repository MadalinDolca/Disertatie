package com.madalin.disertatie.auth.presentation.actions

sealed class RegisterAction {
    data class DoRegistration(val email: String, val password: String, val confirmPassword: String) : RegisterAction()
    data object ResetRegistrationStatus : RegisterAction()
    data object HideStatusDialog : RegisterAction()
}