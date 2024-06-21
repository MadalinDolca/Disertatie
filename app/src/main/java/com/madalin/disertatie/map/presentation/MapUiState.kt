package com.madalin.disertatie.map.presentation

import android.location.Location
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.map.presentation.components.SuggestionDialogState

data class MapUiState(
    val currentUser: User? = null,
    val isLocationAvailable: Boolean = true,
    val currentUserLocation: Location? = null,

    // trail creation
    val currentTrail: Trail? = null,
    val isCreatingTrail: Boolean = false,
    val isCreatingTrailPaused: Boolean = false,
    val isTrailEndDialogVisible: Boolean = false,
    val isTrailUploading: Boolean = false,
    val trailNameError: UiText = UiText.Empty,

    // nearby trails
    val nearbyTrails: List<Trail> = emptyList(),
    val areNearbyTrailsVisible: Boolean = false,

    // trail point info modal bottom sheet
    val selectedTrailPoint: TrailPoint? = null,
    val isTrailPointInfoModalVisible: Boolean = false,

    // launched trail
    val isLaunchedTrail: Boolean = false,
    val isLoadingLaunchedTrail: Boolean = false,
    val launchedTrailId: String? = null,

    // weather
    val isLoadingWeather: Boolean = false,

    // suggestion
    val isActivitySuggestionsDialogVisible: Boolean = false,
    val isLoadingSuggestion: Boolean = false,
    val suggestionDialogState: SuggestionDialogState = SuggestionDialogState(),

    // camera and map
    val cameraPositionState: CameraPositionState = CameraPositionState(),
    val mapProperties: MapProperties = MapProperties(isMyLocationEnabled = false),
    val mapUiSettings: MapUiSettings = MapUiSettings(
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false,
        zoomControlsEnabled = false
    )
)