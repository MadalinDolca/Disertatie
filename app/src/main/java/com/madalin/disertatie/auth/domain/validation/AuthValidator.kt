package com.madalin.disertatie.auth.domain.validation

import android.util.Patterns

object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 40

    /**
     * Validates the given [email], [password] and [confirmPassword] using [validateEmail],
     * [validatePassword] and [validateConfirmPassword] and returns a list list of possible errors.
     */
    fun validateRegistrationFields(email: String, password: String, confirmPassword: String): List<Any> {
        val errors = mutableListOf<Any>()
        errors.apply {
            addAll(validateEmail(email))
            addAll(validatePassword(password))
            addAll(validateConfirmPassword(password, confirmPassword))
        }
        return errors
    }

    /**
     * Validates the given [email] and [password] using [validateEmail] and [validatePassword] and
     * returns a list list of possible errors.
     */
    fun validateLoginFields(email: String, password: String): List<Any> {
        val errors = mutableListOf<Any>()
        errors.apply {
            addAll(validateEmail(email))
            addAll(validatePassword(password))
        }
        return errors
    }

    /**
     * Validates the given [email] and returns a list of possible [EmailResult]s.
     */
    fun validateEmail(email: String): List<Any> {
        val errors = mutableListOf<Any>()

        if (email.isEmpty()) { // no email provided
            errors.add(EmailResult.NoEmailProvided)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // invalid email address
            errors.add(EmailResult.InvalidEmail)
        }

        return errors
    }

    /**
     * Validates the given [password] and returns a list of possible [PasswordResult]s.
     */
    fun validatePassword(password: String): List<Any> {
        val errors = mutableListOf<Any>()

        if (password.isEmpty()) { // no password provided
            errors.add(PasswordResult.NoPasswordProvided)
        } else if (password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
            errors.add(PasswordResult.InvalidPasswordLength)
        }

        return errors
    }

    /**
     * Validates the given [password] and [confirmPassword] combo and returns a list of possible
     * [ConfirmPasswordResult]s.
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): List<Any> {
        val errors = mutableListOf<Any>()

        if (confirmPassword.isEmpty()) { // no confirm password provided
            errors.add(ConfirmPasswordResult.NoPasswordProvided)
        } else if (password != confirmPassword) { // passwords don't match
            errors.add(ConfirmPasswordResult.PasswordsNotMatching)
        }

        return errors
    }

    sealed class EmailResult {
        data object NoEmailProvided : EmailResult()
        data object InvalidEmail : EmailResult()
    }

    sealed class PasswordResult {
        data object NoPasswordProvided : PasswordResult()
        data object InvalidPasswordLength : PasswordResult()
    }

    sealed class ConfirmPasswordResult {
        data object NoPasswordProvided : ConfirmPasswordResult()
        data object PasswordsNotMatching : ConfirmPasswordResult()
    }
}