package com.madalin.disertatie.auth.domain.validation

import android.util.Patterns
import com.madalin.disertatie.core.domain.util.LengthConstraint

/**
 * Validates the given [email], [password] and [confirmPassword] using [validateEmail],
 * [validatePassword] and [validateConfirmPassword] and returns a list list of possible errors.
 */
fun validateRegisterFields(email: String, password: String, confirmPassword: String): List<Any> {
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
 * Validates the given [email] and returns a list of possible [EmailFieldError]s.
 */
fun validateEmail(email: String): List<Any> {
    val errors = mutableListOf<Any>()

    if (email.isEmpty()) { // no email provided
        errors.add(EmailFieldError.NoEmailProvided)
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // invalid email address
        errors.add(EmailFieldError.InvalidEmail)
    }

    return errors
}

/**
 * Validates the given [password] and returns a list of possible [PasswordFieldError]s.
 */
fun validatePassword(password: String): List<Any> {
    val errors = mutableListOf<Any>()

    if (password.isEmpty()) { // no password provided
        errors.add(PasswordFieldError.NoPasswordProvided)
    } else if (password.length < LengthConstraint.MIN_PASSWORD_LENGTH) {
        errors.add(PasswordFieldError.InvalidPasswordLength)
    }

    return errors
}

/**
 * Validates the given [password] and [confirmPassword] combo and returns a list of possible
 * [ConfirmPasswordFieldError]s.
 */
fun validateConfirmPassword(password: String, confirmPassword: String): List<Any> {
    val errors = mutableListOf<Any>()

    if (confirmPassword.isEmpty()) { // no confirm password provided
        errors.add(ConfirmPasswordFieldError.NoPasswordProvided)
    } else if (password != confirmPassword) { // passwords don't match
        errors.add(ConfirmPasswordFieldError.PasswordsNotMatching)
    }

    return errors
}