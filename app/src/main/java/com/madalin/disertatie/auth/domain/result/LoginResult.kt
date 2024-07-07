package com.madalin.disertatie.auth.domain.result

sealed class LoginResult {
    data object Success : LoginResult()
    data object UserNotFound : LoginResult()
    data object InvalidCredentials : LoginResult()
    data class Error(val message: String? = null) : LoginResult()
}