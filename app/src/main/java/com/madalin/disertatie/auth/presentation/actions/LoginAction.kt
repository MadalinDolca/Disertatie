package com.madalin.disertatie.auth.presentation.actions

sealed class LoginAction {
    data class DoLogin(val email: String, val password: String) : LoginAction()
    data object ResetLoginStatus : LoginAction()
    data object HideStatusBanner : LoginAction()
}