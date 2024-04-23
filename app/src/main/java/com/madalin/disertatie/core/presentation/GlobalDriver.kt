package com.madalin.disertatie.core.presentation

import android.util.Log
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.domain.failures.UserFailure
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GlobalDriver(
    private val userRepository: FirebaseUserRepository
) {
    private val dispatcher = newSingleThreadContext("AppContext")
    private val _state = MutableStateFlow(GlobalState()) // the overall state of the application
    val state = _state.asSharedFlow() // shared state to allow components to observe the state and react to state changes

    /**
     * Determines the [action] type and calls the appropriate handle method.
     */
    fun handleAction(action: GlobalAction) = runBlocking {
        withContext(dispatcher) {
            when (action) {
                is GlobalAction.SetUserLoginStatus -> toggleUserLoginStatus(action.isLoggedIn)
                GlobalAction.ListenForUserData -> listenForUserData()
                is GlobalAction.SetStatusBannerData -> setStatusBannerData(action.data)
                GlobalAction.ShowStatusBanner -> toggleStatusBannerVisibility(true)
                GlobalAction.HideStatusBanner -> toggleStatusBannerVisibility(false)
            }
        }
    }

    /**
     * Checks if the current user is signed in and obtains the stored user ID.
     * @return `true` if the user is signed in and has ID, `false` otherwise
     */
    private fun isUserSignedIn(): Boolean {
        if (userRepository.isSignedIn()) {
            val userId = userRepository.getCurrentUserId()

            if (userId != null) {
                _state.update { currentState ->
                    currentState.copy(
                        isUserLoggedIn = true,
                        currentUser = currentState.currentUser.copy(
                            id = userId
                        )
                    )
                }
                return true
            }
        }
        return false
    }

    /**
     * Starts listening for user data changes and updates user [_state].
     */
    private fun startListeningForUserData() {
        userRepository.startListeningForUserData(
            onSuccess = { fetchedUserData ->
                Log.d("AppStateDriver", "Obtained new user data: $fetchedUserData")
                _state.update { it.copy(currentUser = fetchedUserData) }
            },
            onFailure = { failureType ->
                Log.e("AppStateDriver", "Failed to obtain new user data")
                _state.update { currentState ->
                    currentState.copy(
                        isStatusBannerVisible = true,
                        statusBannerData = StatusBannerData(
                            StatusBannerType.Error,
                            determineUserDataFetchingFailureMessage(failureType)
                        )
                    )
                }
            }
        )
    }

    /**
     * Determines the [failureType] and returns the specific [UiText] message.
     */
    private fun determineUserDataFetchingFailureMessage(failureType: UserFailure) = when (failureType) {
        UserFailure.DataFetchingError -> UiText.Resource(R.string.data_fetching_error)
        UserFailure.NoUserId -> UiText.Resource(R.string.could_not_get_the_user_id)
        UserFailure.UserDataNotFound -> UiText.Resource(R.string.user_data_not_found)
    }

    /**
     * Starts listening for user data via [startListeningForUserData] if the user is logged in via [isUserSignedIn]
     */
    private fun listenForUserData() {
        if (isUserSignedIn()) {
            startListeningForUserData()
        }
    }

    private fun setStatusBannerData(data: StatusBannerData) {
        _state.update { it.copy(statusBannerData = data) }
    }

    private fun toggleStatusBannerVisibility(isVisible: Boolean) {
        _state.update { it.copy(isStatusBannerVisible = isVisible) }
    }

    private fun toggleUserLoginStatus(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            _state.update { it.copy(isUserLoggedIn = true) }
        } else {
            userRepository.signOut(
                onSuccess = {
                    _state.update { it.copy(isUserLoggedIn = false) }
                },
                onFailure = { errorMessage ->
                    _state.update {
                        it.copy(
                            isStatusBannerVisible = true,
                            statusBannerData = StatusBannerData(
                                StatusBannerType.Error,
                                if (errorMessage != null) UiText.Dynamic(errorMessage) else UiText.Empty
                            )
                        )
                    }
                }
            )
        }
    }
}