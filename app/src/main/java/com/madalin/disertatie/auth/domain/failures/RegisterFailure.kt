package com.madalin.disertatie.auth.domain.failures

/**
 * Registration failure types.
 */
sealed class RegisterFailure {
    data object InvalidEmail : RegisterFailure()
    data object InvalidCredentials : RegisterFailure()
    data object EmailIsTaken : RegisterFailure()
    data class Error(val message: String? = null) : RegisterFailure()
}