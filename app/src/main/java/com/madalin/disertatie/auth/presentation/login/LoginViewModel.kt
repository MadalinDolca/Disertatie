package com.madalin.disertatie.auth.presentation.login

import androidx.lifecycle.ViewModel
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.failures.LoginFailure
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.validation.EmailFieldError
import com.madalin.disertatie.auth.domain.validation.PasswordFieldError
import com.madalin.disertatie.auth.domain.validation.validateLoginFields
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.domain.util.LengthConstraint
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Logs in the user with the given [email] and [password] if they are valid. Checks if the
     * [email] is verified and updates [_uiState].
     */
    fun login(email: String, password: String) {
        val _email = email.trim()
        val _password = password.trim()

        // if the given data is not valid then the login fails
        if (!validateFields(_email, _password)) return

        // proceeds to login
        repository.signInWithEmailAndPassword(_email, _password,
            onSuccess = {
                if (repository.isEmailVerified()) {
                    _uiState.update { it.copy(isLoginOperationComplete = true) }
                    globalDriver.handleAction(GlobalAction.SetUserLoginStatus(true))
                } else {
                    repository.sendEmailVerification()
                    _uiState.update {
                        it.copy(
                            isLoginOperationComplete = false,
                            isStatusBannerVisible = true,
                            statusBannerData = StatusBannerData(
                                StatusBannerType.Info,
                                UiText.Resource(R.string.check_your_email_to_confirm_your_account)
                            )
                        )
                    }
                }
            },
            onFailure = { failureType ->
                _uiState.update {
                    it.copy(
                        isStatusBannerVisible = true,
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Error,
                            determineLoginFailureMessage(failureType)
                        )
                    )
                }
            }
        )
    }

    /**
     * Checks if the given [email] and [password] are valid. If not, it updates [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String): Boolean {
        val errors = validateLoginFields(email, password)

        _uiState.update { currentState ->
            currentState.copy(
                emailError = when (errors.find { it is EmailFieldError } as? EmailFieldError) {
                    EmailFieldError.NoEmailProvided -> UiText.Resource(R.string.email_cant_be_empty)
                    EmailFieldError.InvalidEmail -> UiText.Resource(R.string.email_is_invalid)
                    null -> UiText.Empty
                },
                passwordError = when (errors.find { it is PasswordFieldError } as? PasswordFieldError) {
                    PasswordFieldError.NoPasswordProvided -> UiText.Resource(R.string.password_cant_be_empty)
                    PasswordFieldError.InvalidPasswordLength -> UiText.Resource(
                        R.string.password_must_be_between_x_and_y_characters,
                        LengthConstraint.MIN_PASSWORD_LENGTH,
                        LengthConstraint.MAX_PASSWORD_LENGTH
                    )

                    null -> UiText.Empty
                }
            )
        }

        return errors.isEmpty()
    }

    /**
     * Determines the [failureType] and returns the specific [UiText] message.
     */
    private fun determineLoginFailureMessage(failureType: LoginFailure) = when (failureType) {
        LoginFailure.InvalidCredentials -> UiText.Resource(R.string.invalid_credentials)
        LoginFailure.UserNotFound -> UiText.Resource(R.string.user_not_found)
        is LoginFailure.Error -> {
            if (failureType.message != null) UiText.Dynamic(failureType.message)
            else UiText.Resource(R.string.login_error)
        }
    }

    /**
     * Sets [login operation status][LoginUiState.isLoginOperationComplete] to [status].
     * @param status `true` if user is signed in, `false` otherwise
     */
    fun setLoginOperationStatus(status: Boolean) {
        _uiState.update { it.copy(isLoginOperationComplete = status) }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun toggleStatusBannerVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isStatusBannerVisible = isVisible) }
    }
}