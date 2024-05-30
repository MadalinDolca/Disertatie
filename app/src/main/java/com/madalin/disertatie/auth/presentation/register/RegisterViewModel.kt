package com.madalin.disertatie.auth.presentation.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.validation.ConfirmPasswordFieldError
import com.madalin.disertatie.auth.domain.validation.EmailFieldError
import com.madalin.disertatie.auth.domain.validation.PasswordFieldError
import com.madalin.disertatie.auth.domain.validation.validateRegisterFields
import com.madalin.disertatie.auth.domain.failures.RegisterFailure
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.domain.util.LengthConstraint
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.components.StatusDialogData
import com.madalin.disertatie.core.presentation.components.StatusDialogType
import com.madalin.disertatie.core.presentation.util.UiText
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
     * Registers the user with the given [email] and [password] if they are valid.
     * If the user creation has been successfully performed, calls [storeUserToFirestore] to store
     * its data.
     */
    fun register(email: String, password: String, confirmPassword: String) {
        val _email = email.trim()
        val _password = password.trim()
        val _confirmPassword = confirmPassword.trim()

        // if the given data is not valid then the registration fails
        if (!validateFields(_email, _password, _confirmPassword)) return

        // otherwise show the dialog
        _uiState.update {
            it.copy(
                isStatusDialogVisible = true,
                statusDialogData = StatusDialogData(
                    StatusDialogType.Processing,
                    UiText.Resource(R.string.processing)
                )
            )
        }

        // proceed to registration
        repository.createUserWithEmailAndPassword(_email, _password,
            onSuccess = { firebaseUser ->
                firebaseUser?.let { // if the current user is not null
                    storeUserToFirestore(User(id = it.uid, email = _email))
                }
            },
            onFailure = { failureType ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isRegisterOperationComplete = false,
                        isStatusDialogVisible = false, // dismiss the dialog
                        isStatusBannerVisible = true, // show the banner
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Error,
                            determineRegisterFailureMessage(failureType)
                        )
                    )
                }
            }
        )
    }

    /**
     * Checks if the given [email], [password] and [confirmPassword] are valid. If not, it updates
     * [_uiState] accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
        val errors = validateRegisterFields(email, password, confirmPassword)

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
                },
                confirmPasswordError = when (errors.find { it is ConfirmPasswordFieldError } as? ConfirmPasswordFieldError) {
                    ConfirmPasswordFieldError.NoPasswordProvided -> UiText.Resource(R.string.password_cant_be_empty)
                    ConfirmPasswordFieldError.PasswordsNotMatching -> UiText.Resource(R.string.passwords_dont_match)
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
        repository.storeAccountDataToFirestore(user,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        statusDialogData = StatusDialogData( // update the dialog
                            StatusDialogType.Success,
                            UiText.Resource(R.string.you_have_successfully_registered)
                        )
                    )
                }

                viewModelScope.launch {
                    delay(3000)
                    _uiState.update {
                        it.copy(
                            isStatusDialogVisible = false, // dismiss the dialog
                            isRegisterOperationComplete = true // registered successfully
                        )
                    }
                }
            },
            onFailure = { errorMessage ->
                errorMessage?.let { Log.e("SignUpViewModel", it) }
                _uiState.update {
                    it.copy(
                        isStatusDialogVisible = false, // dismiss the dialog
                        isStatusBannerVisible = true, // show the banner
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Error, if (errorMessage != null) {
                                UiText.Dynamic(errorMessage)
                            } else {
                                UiText.Resource(R.string.data_recording_error)
                            }
                        )
                    )
                }
            })
    }

    /**
     * Determines the [failureType] and returns the specific [UiText] message.
     */
    private fun determineRegisterFailureMessage(failureType: RegisterFailure) = when (failureType) {
        RegisterFailure.InvalidEmail -> UiText.Resource(R.string.email_is_invalid)
        RegisterFailure.InvalidCredentials -> UiText.Resource(R.string.invalid_credentials)
        RegisterFailure.EmailIsTaken -> UiText.Resource(R.string.email_is_taken)
        is RegisterFailure.Error -> {
            if (failureType.message != null) UiText.Dynamic(failureType.message)
            else UiText.Resource(R.string.error_creating_account)
        }
    }

    /**
     * Sets the [register operation status][RegisterUiState.isRegisterOperationComplete] to [status].
     * @param status `true` if user has signed up, `false` otherwise
     */
    fun setRegisterOperationStatus(status: Boolean) {
        _uiState.update {
            it.copy(isRegisterOperationComplete = status)
        }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun setConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun toggleStatusDialogVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isStatusDialogVisible = isVisible) }
    }

    fun toggleStatusBannerVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isStatusBannerVisible = isVisible) }
    }
}