package com.madalin.disertatie.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.result.AccountDataStorageResult
import com.madalin.disertatie.auth.domain.result.RegisterResult
import com.madalin.disertatie.auth.domain.validation.AuthValidator
import com.madalin.disertatie.auth.presentation.actions.RegisterAction
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.components.StatusDialogData
import com.madalin.disertatie.core.presentation.components.StatusDialogType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Handles the given [RegisterAction] by calling the appropriate handle method.
     */
    fun handleAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.DoRegistration -> register(action.email, action.password, action.confirmPassword)
            RegisterAction.ResetRegistrationStatus -> resetRegistrationStatus()
            RegisterAction.HideStatusBanner -> hideStatusBanner()
            RegisterAction.HideStatusDialog -> hideStatusDialog()
        }
    }

    /**
     * Registers the user with the given [email] and [password] if they are valid.
     * If the user creation has been successfully performed, calls [storeUserToFirestore] to store
     * its data.
     */
    private fun register(email: String, password: String, confirmPassword: String) {
        val _email = email.trim()
        val _password = password.trim()
        val _confirmPassword = confirmPassword.trim()

        // if the given data is not valid then the registration fails
        if (!validateFields(_email, _password, _confirmPassword)) return

        // otherwise proceeds to registration
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStatusDialogVisible = true,
                    statusDialogData = StatusDialogData(StatusDialogType.Processing, UiText.Resource(R.string.processing))
                )
            }

            val result = async { repository.createUserWithEmailAndPassword(_email, _password) }.await()
            when (result) {
                // TODO what if the user is null?
                is RegisterResult.Success -> result.firebaseUser?.let { // if the current user is not null
                    storeUserToFirestore(User(id = it.uid, email = _email))
                }

                RegisterResult.InvalidEmail -> updateStateUponFailure(UiText.Resource(R.string.email_is_invalid))
                RegisterResult.InvalidCredentials -> updateStateUponFailure(UiText.Resource(R.string.invalid_credentials))
                RegisterResult.EmailIsTaken -> updateStateUponFailure(UiText.Resource(R.string.email_is_taken))
                is RegisterResult.Error -> updateStateUponFailure(
                    if (result.message != null) UiText.Dynamic(result.message)
                    else UiText.Resource(R.string.error_creating_account)
                )
            }
        }
    }

    /**
     * Checks if the given [email], [password] and [confirmPassword] are valid. If not, it updates
     * [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
        val errors = AuthValidator.validateRegistrationFields(email, password, confirmPassword)

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
                },
                confirmPasswordError = when (errors.find { it is AuthValidator.ConfirmPasswordResult } as? AuthValidator.ConfirmPasswordResult) {
                    AuthValidator.ConfirmPasswordResult.NoPasswordProvided -> UiText.Resource(R.string.password_cant_be_empty)
                    AuthValidator.ConfirmPasswordResult.PasswordsNotMatching -> UiText.Resource(R.string.passwords_dont_match)
                    null -> UiText.Empty
                }
            )
        }

        return errors.isEmpty() // invalid credentials if there are errors
    }

    /**
     * Stores the given [user] data to Firestore and updates [_uiState].
     */
    private fun storeUserToFirestore(user: User) {
        viewModelScope.launch {
            val result = async { repository.storeAccountDataToFirestore(user) }.await()
            when (result) {
                AccountDataStorageResult.Success -> {
                    _uiState.update {
                        it.copy(
                            statusDialogData = StatusDialogData( // updates the dialog
                                StatusDialogType.Success,
                                UiText.Resource(R.string.you_have_successfully_registered)
                            )
                        )
                    }
                    delay(3000)
                    _uiState.update {
                        it.copy(
                            isStatusDialogVisible = false, // dismiss the dialog
                            isRegisterOperationComplete = true // registered successfully
                        )
                    }
                }

                is AccountDataStorageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isStatusDialogVisible = false, // dismiss the dialog
                            isStatusBannerVisible = true, // shows the banner
                            statusBannerData = StatusBannerData(
                                StatusBannerType.Error,
                                if (result.message != null) {
                                    UiText.Dynamic(result.message)
                                } else {
                                    UiText.Resource(R.string.data_recording_error)
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the state upon a registration failure with the given [UiText] message.
     */
    private fun updateStateUponFailure(text: UiText) {
        _uiState.update { currentState ->
            currentState.copy(
                isRegisterOperationComplete = false,
                isStatusDialogVisible = false, // dismiss the dialog
                isStatusBannerVisible = true, // show the banner
                statusBannerData = StatusBannerData(StatusBannerType.Error, text)
            )
        }
    }

    /**
     * Resets the [register operation status][RegisterUiState.isRegisterOperationComplete] by
     * setting it to `false`.
     */
    private fun resetRegistrationStatus() {
        _uiState.update { it.copy(isRegisterOperationComplete = false) }
    }

    /**
     * Hides the status banner.
     */
    private fun hideStatusBanner() {
        _uiState.update { it.copy(isStatusBannerVisible = false) }
    }

    /**
     * Hides the status dialog.
     */
    private fun hideStatusDialog() {
        _uiState.update { it.copy(isStatusDialogVisible = false) }
    }
}