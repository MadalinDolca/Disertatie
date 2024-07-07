package com.madalin.disertatie.discover.presentation

import android.location.Location
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.util.UiText

data class DiscoverUiState(
    // current user
    val currentUser: User = User(),
    val currentUserLocation: Location? = null,

    // trails
    val isLoadingTrails: Boolean = false,
    val discoverTrails: List<Trail> = emptyList(),
    val discoverTrailsError: UiText = UiText.Empty,

    // searched trails
    val isSearchingTrails: Boolean = false,
    val searchedTrails: List<Trail> = emptyList(),
    val searchError: UiText = UiText.Empty,

    // nearby trails
    val isLoadingNearbyTrails: Boolean = false,
    val nearbyTrails: List<Trail> = emptyList(),
    val nearbyTrailsError: UiText = UiText.Empty
)