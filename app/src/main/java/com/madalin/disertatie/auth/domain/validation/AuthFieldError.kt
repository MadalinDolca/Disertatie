package com.madalin.disertatie.auth.domain.validation

sealed class EmailFieldError {
    data object NoEmailProvided : EmailFieldError()
    data object InvalidEmail : EmailFieldError()
}

sealed class PasswordFieldError {
    data object NoPasswordProvided : PasswordFieldError()
    data object InvalidPasswordLength : PasswordFieldError()
}

sealed class ConfirmPasswordFieldError {
    data object NoPasswordProvided : ConfirmPasswordFieldError()
    data object PasswordsNotMatching : ConfirmPasswordFieldError()
}