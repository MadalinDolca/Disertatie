package com.madalin.disertatie.auth.presentation.password_reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.result.PasswordResetResult
import com.madalin.disertatie.auth.domain.validation.AuthValidator
import com.madalin.disertatie.auth.presentation.actions.PasswordResetAction
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Handles the given [PasswordResetAction] by calling the appropriate handle method.
     */
    fun handleAction(action: PasswordResetAction) {
        when (action) {
            is PasswordResetAction.ResetPassword -> resetPassword(action.email)
        }
    }

    /**
     * Resets the user password associated with the given [email] and updates [_uiState].
     */
    private fun resetPassword(email: String) {
        val _email = email.trim()

        // if the given data is not valid then the password reset fails
        if (!validateField(_email)) return

        // otherwise proceeds to reset
        viewModelScope.launch {
            val result = async { repository.resetPassword(_email) }.await()
            when (result) {
                is PasswordResetResult.Success -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(
                        StatusBannerType.Success,
                        UiText.Resource(R.string.a_password_reset_link_has_been_sent_to_x, result.email)
                    )
                )

                is PasswordResetResult.Error -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(
                        StatusBannerType.Error,
                        UiText.Resource(R.string.something_went_wrong_please_try_again)
                    )
                )
            }
        }
    }

    /**
     * Checks if the given [email] is valid. If not, it updates [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateField(email: String): Boolean {
        val errors = AuthValidator.validateEmail(email)

        _uiState.update { currentState ->
            currentState.copy(
                emailError = when (errors.find { it is AuthValidator.EmailResult } as? AuthValidator.EmailResult) {
                    AuthValidator.EmailResult.NoEmailProvided -> UiText.Resource(R.string.email_cant_be_empty)
                    AuthValidator.EmailResult.InvalidEmail -> UiText.Resource(R.string.email_is_invalid)
                    null -> UiText.Empty
                }
            )
        }
        return errors.isEmpty()
    }
}