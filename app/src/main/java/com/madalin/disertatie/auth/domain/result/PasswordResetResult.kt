package com.madalin.disertatie.auth.domain.result

sealed class PasswordResetResult {
    data class Success(val email: String) : PasswordResetResult()
    data class Error(val message: String?) : PasswordResetResult()
}