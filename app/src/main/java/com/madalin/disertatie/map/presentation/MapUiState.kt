package com.madalin.disertatie.map.presentation

import android.location.Location
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint

data class MapUiState(
    val isLocationAvailable: Boolean = true,
    val isCreatingTrail: Boolean = false,
    val isCreatingTrailPaused: Boolean = false,

    val currentUserLocation: Location? = null,
    val currentTrail: Trail? = null,
    val userTrails: MutableList<Trail> = mutableListOf(),
    val remoteTrails: MutableList<Trail> = mutableListOf(),

    // trail point info modal bottom sheet
    val selectedTrailPoint: TrailPoint? = null,
    val isTrailPointInfoModalVisible: Boolean = false,

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