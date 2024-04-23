package com.madalin.disertatie.auth.domain.failures

/**
 * Login failure types.
 */
sealed class LoginFailure {
    data object UserNotFound : LoginFailure()
    data object InvalidCredentials : LoginFailure()
    data class Error(val message: String? = null) : LoginFailure()
}