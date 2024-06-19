package com.madalin.disertatie.core.presentation

import android.util.Log
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.domain.result.UserResult
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GlobalDriver(
    private val userRepository: FirebaseUserRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val dispatcher = newSingleThreadContext("AppContext")
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(GlobalState()) // the overall state of the application
    val state = _state.asSharedFlow() // shared state to allow components to observe the state and react to state changes

    /**
     * Determines the global [action] type and calls the appropriate handle method.
     */
    fun handleAction(action: GlobalAction) = runBlocking {
        withContext(dispatcher) {
            when (action) {
                is GlobalAction.SetUserLoginStatus -> toggleUserLoginStatus(action.isLoggedIn)
                GlobalAction.ListenForUserData -> listenForUserData()
                is GlobalAction.SetStatusBannerData -> setStatusBannerData(action.data)
                GlobalAction.ShowStatusBanner -> toggleStatusBannerVisibility(true)
                GlobalAction.HideStatusBanner -> toggleStatusBannerVisibility(false)
                is GlobalAction.SetLaunchedTrailId -> setLaunchedTrailId(action.id)
                GlobalAction.DeleteLaunchedTrailId -> deleteLaunchedTrailId()
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
     * Starts listening for user data changes and updates the user [_state].
     */
    private fun startListeningForUserData() {
        scope.launch {
            val result = launch {
                userRepository.observerUserData()
                    .collect { result ->
                        when (result) {
                            is UserResult.Success -> {
                                Log.d("GlobalDriver", "Obtained new user data: ${result.user}")
                                _state.update { it.copy(currentUser = result.user) }
                            }

                            UserResult.NoUserId, UserResult.DataFetchingError, UserResult.UserDataNotFound ->
                                _state.update {
                                    it.copy(
                                        isStatusBannerVisible = true,
                                        statusBannerData = StatusBannerData(
                                            StatusBannerType.Error,
                                            determineUserDataFetchingFailureMessage(result)
                                        )
                                    )
                                }
                        }
                    }
            }
            result.join() // keeps the coroutine running to listen for updates
        }
    }

    /**
     * Determines the [failureType] and returns the specific [UiText] message.
     */
    private fun determineUserDataFetchingFailureMessage(failureType: UserResult) = when (failureType) {
        is UserResult.Success -> UiText.Empty
        UserResult.DataFetchingError -> UiText.Resource(R.string.data_fetching_error)
        UserResult.NoUserId -> UiText.Resource(R.string.could_not_get_the_user_id)
        UserResult.UserDataNotFound -> UiText.Resource(R.string.user_data_not_found)
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

    /**
     * Sets the ID of the currently launched trail to the given [id].
     */
    private fun setLaunchedTrailId(id: String) {
        _state.update { it.copy(launchedTrailId = id) }
    }

    /**
     * Deletes the ID of the currently launched trail by making it `null`.
     */
    private fun deleteLaunchedTrailId() {
        _state.update { it.copy(launchedTrailId = null) }
    }
}