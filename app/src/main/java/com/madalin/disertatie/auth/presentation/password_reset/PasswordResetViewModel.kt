package com.madalin.disertatie.auth.presentation.password_reset

import androidx.lifecycle.ViewModel
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.validation.EmailFieldError
import com.madalin.disertatie.auth.domain.validation.validateEmail
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PasswordResetViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Resets the user password associated with the given [email] and updates [_uiState].
     */
    fun resetPassword(email: String) {
        val _email = email.trim()

        // if the given data is not valid then the password reset fails
        if (!validateField(_email)) return

        // otherwise proceeds to reset
        repository.resetPassword(_email,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isStatusBannerVisible = true,
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Success,
                            UiText.Resource(R.string.a_password_reset_link_has_been_sent_to_x, _uiState.value.email)
                        )
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        isStatusBannerVisible = true,
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Error,
                            UiText.Resource(R.string.something_went_wrong_please_try_again)
                        )
                    )
                }
            }
        )
    }

    /**
     * Checks if the given [email] is valid. If not, it updates [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateField(email: String): Boolean {
        val errors = validateEmail(email)

        _uiState.update { currentState ->
            currentState.copy(
                emailError = when (errors.find { it is EmailFieldError } as? EmailFieldError) {
                    EmailFieldError.NoEmailProvided -> UiText.Resource(R.string.email_cant_be_empty)
                    EmailFieldError.InvalidEmail -> UiText.Resource(R.string.email_is_invalid)
                    null -> UiText.Empty
                })
        }

        return errors.isEmpty()
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun toggleStatusBannerVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isStatusBannerVisible = isVisible) }
    }
}