package com.madalin.disertatie.home.presentation

import android.location.Location
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.madalin.disertatie.home.domain.model.TrailPoint

data class HomeUiState(
    val isLocationAvailable: Boolean = false,
    val isCreatingTrail: Boolean = false,

    val trailPointsList: List<TrailPoint> = emptyList(),
    val currentUserLocation: Location? = null,

    val cameraPositionState: CameraPositionState = CameraPositionState(),
    val mapProperties: MapProperties = MapProperties(isMyLocationEnabled = true),
    val mapUiSettings: MapUiSettings = MapUiSettings(zoomControlsEnabled = false),
    val isCameraDragged: Boolean = false
)