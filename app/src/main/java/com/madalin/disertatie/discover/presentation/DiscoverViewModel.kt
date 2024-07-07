package com.madalin.disertatie.discover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.result.TrailsListResult
import com.madalin.disertatie.core.domain.util.NEARBY_TRAIL_MIN_DISTANCE
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.discover.presentation.action.DiscoverAction
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val globalDriver: GlobalDriver,
    private val firebaseContentRepository: FirebaseContentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    private var hasSearchedForNearbyTrails = false

    init {
        // collects global state and obtains nearby trails if the user location is not null
        // and hasn't searched for them yet
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce().run {
                    if (_uiState.value.currentUserLocation != null && !hasSearchedForNearbyTrails) {
                        getNearbyTrails()
                    }
                }
            }
        }

        // get trails
        getLimitedTrails()
    }

    /**
     * Reduces this [GlobalState] into the [DiscoverUiState].
     */
    private fun GlobalState.reduce() {
        _uiState.update { currentState ->
            currentState.copy(
                currentUser = this.currentUser,
                currentUserLocation = this.currentUserLocation
            )
        }
    }

    /**
     * Handles the given [DiscoverAction] by calling the appropriate handle method.
     */
    fun handleAction(action: DiscoverAction) {
        when (action) {
            DiscoverAction.RefreshNearbyTrails -> getNearbyTrails()
            DiscoverAction.RefreshLimitedTrails -> getLimitedTrails()
            is DiscoverAction.Search -> searchTrails(action.query)
        }
    }

    /**
     * Gets the nearby trails by the current user location.
     */
    private fun getNearbyTrails() {
        val currentUserLocation = _uiState.value.currentUserLocation
        if (currentUserLocation == null) {
            _uiState.update { it.copy(nearbyTrailsError = UiText.Resource(R.string.could_not_get_the_nearby_trails_because_the_user_location_is_null)) }
            return
        }

        if (_uiState.value.isLoadingNearbyTrails) {
            globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Info, R.string.already_looking_for_nearby_trails_please_wait))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNearbyTrails = true) }

            val result = async { firebaseContentRepository.getPublicNearbyTrailsByLocation(currentUserLocation, NEARBY_TRAIL_MIN_DISTANCE) }.await()
            hasSearchedForNearbyTrails = true // first time fetched

            when (result) {
                is TrailsListResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingNearbyTrails = false,
                            nearbyTrails = result.trails,
                            nearbyTrailsError = UiText.Empty
                        )
                    }
                }

                is TrailsListResult.Error -> {
                    globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_get_the_nearby_trails))
                    _uiState.update {
                        it.copy(
                            isLoadingNearbyTrails = false,
                            nearbyTrails = emptyList(),
                            nearbyTrailsError = UiText.Empty
                        )
                    }
                }
            }
        }
    }

    /**
     * Gets a [limited][limit] number of trails. Default limit is `10`.
     */
    private fun getLimitedTrails(limit: Long = 10) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrails = true) }

            val response = async { firebaseContentRepository.getPublicTrailsWithLimit(limit) }.await()
            when (response) {
                is TrailsListResult.Success -> _uiState.update {
                    it.copy(
                        isLoadingTrails = false,
                        discoverTrails = response.trails,
                        discoverTrailsError = UiText.Empty
                    )
                }

                is TrailsListResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingTrails = false,
                            discoverTrails = emptyList(),
                            discoverTrailsError = if (response.error != null) {
                                UiText.Dynamic(response.error)
                            } else {
                                UiText.Resource(R.string.could_not_get_trails)
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Searches for trails by the given [query].
     */
    private fun searchTrails(query: String) {
        val formattedQuery = query.trim().lowercase()

        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingTrails = true) }

            val response = async { firebaseContentRepository.getPublicTrailsByQuery(formattedQuery) }.await()
            when (response) {
                is TrailsListResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSearchingTrails = false,
                            searchedTrails = response.trails,
                            searchError = UiText.Empty
                        )
                    }
                }

                is TrailsListResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearchingTrails = false,
                            searchedTrails = emptyList(),
                            searchError = if (response.error != null) {
                                UiText.Dynamic(response.error)
                            } else {
                                UiText.Resource(R.string.searching_error)
                            }
                        )
                    }
                }
            }
        }
    }
}