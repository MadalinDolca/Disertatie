package com.madalin.disertatie.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.result.LoginResult
import com.madalin.disertatie.auth.domain.validation.AuthValidator
import com.madalin.disertatie.auth.presentation.actions.LoginAction
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Handles the given [LoginAction] by calling the appropriate handle method.
     */
    fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.DoLogin -> login(action.email, action.password)
            LoginAction.ResetLoginStatus -> resetLoginStatus()
        }
    }

    /**
     * Logs in the user with the given [email] and [password] if they are valid. Checks if the
     * [email] is verified and updates [_uiState].
     */
    private fun login(email: String, password: String) {
        val _email = email.trim()
        val _password = password.trim()

        // if the given data is not valid then the login fails
        if (!validateFields(_email, _password)) return

        // proceeds to login
        viewModelScope.launch {
            val result = async { repository.signInWithEmailAndPassword(_email, _password) }.await()
            when (result) {
                LoginResult.Success -> handleLoginResult()
                LoginResult.InvalidCredentials -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Error, UiText.Resource(R.string.invalid_credentials))
                )

                LoginResult.UserNotFound -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Error, UiText.Resource(R.string.user_not_found))
                )

                is LoginResult.Error -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(
                        StatusBannerType.Error,
                        if (result.message != null) UiText.Dynamic(result.message) else UiText.Resource(R.string.login_error)
                    )
                )
            }
        }
    }

    /**
     * Checks if the given [email] and [password] are valid. If not, it updates [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String): Boolean {
        val errors = AuthValidator.validateLoginFields(email, password)

        _uiState.update { currentState ->
            currentState.copy(
                emailError = when (errors.find { it is AuthValidator.EmailResult } as? AuthValidator.EmailResult) {
                    AuthValidator.EmailResult.NoEmailProvided -> UiText.Resource(R.string.email_cant_be_empty)
                    AuthValidator.EmailResult.InvalidEmail -> UiText.Resource(R.string.email_is_invalid)
                    null -> UiText.Empty
                },
                passwordError = when (errors.find { it is AuthValidator.PasswordResult } as? AuthValidator.PasswordResult) {
                    AuthValidator.PasswordResult.NoPasswordProvided -> UiText.Resource(R.string.password_cant_be_empty)
                    AuthValidator.PasswordResult.InvalidPasswordLength -> UiText.Resource(
                        R.string.password_must_be_between_x_and_y_characters,
                        AuthValidator.MIN_PASSWORD_LENGTH,
                        AuthValidator.MAX_PASSWORD_LENGTH
                    )

                    null -> UiText.Empty
                }
            )
        }

        return errors.isEmpty()
    }

    /**
     * Sends a verification email if the email is not verified, otherwise sets the login status
     * to `true`.
     */
    private fun handleLoginResult() {
        if (repository.isEmailVerified()) {
            globalDriver.onAction(GlobalAction.SetUserLoginStatus(true))
            _uiState.update { it.copy(isLoginOperationComplete = true) }
        } else {
            repository.sendEmailVerification()
            globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Info, UiText.Resource(R.string.check_your_email_to_confirm_your_account)))
            _uiState.update { it.copy(isLoginOperationComplete = false) }
        }
    }

    /**
     * Resets the [login operation status][LoginUiState.isLoginOperationComplete] by setting it to
     * `false`.
     */
    private fun resetLoginStatus() {
        _uiState.update { it.copy(isLoginOperationComplete = false) }
    }
}