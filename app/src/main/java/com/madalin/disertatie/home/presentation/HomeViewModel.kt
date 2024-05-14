package com.madalin.disertatie.home.presentation

import android.content.Context
import android.location.Location
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.home.domain.DefaultLocationClient
import com.madalin.disertatie.home.domain.LocationClient
import com.madalin.disertatie.home.domain.extensions.toLatLng
import com.madalin.disertatie.home.domain.model.TrailPoint
import com.madalin.disertatie.home.domain.requestLocationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val globalDriver: GlobalDriver,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var locationFetchingScope: CoroutineScope
    private lateinit var locationClient: LocationClient

    companion object {
        private const val MIN_DISTANCE = 3
        private const val ZOOM_LEVEL = 18f
    }

    fun enableLocationSettings(
        applicationContext: Context,
        activityResultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        requestLocationSettings(
            context = applicationContext,
            onEnabled = {
                setLocationAvailability(true)
                startLocationFetching(applicationContext)
            },
            onDisabled = {
                activityResultLauncher.launch(it)
            }
        )
    }

    fun startLocationFetching(applicationContext: Context) {
        locationFetchingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        locationClient
            .getLocationUpdates(1000L)
            .catch {
                handleUserLocatingException(it)
                handleTrailCreationException(it)
            }
            .onEach { location ->
                if (location != null) {
                    handleLocationAvailable(location)
                } else {
                    handleLocationNotAvailable()
                }
            }
            .launchIn(locationFetchingScope)
    }

    fun stopLocationFetching() {
        locationFetchingScope.cancel()
    }

    private fun handleLocationAvailable(location: Location) {
        val lastRegisteredLocation = _uiState.value.currentUserLocation

        // sets the location availability state to true only if it was false before to avoid unnecessary updates
        if (!_uiState.value.isLocationAvailable) setLocationAvailability(true)

        // if no trail point has been registered yet or if the distance between the last registered
        // location and the new location is longer than the minimum distance, then save the new location
        if (lastRegisteredLocation == null || lastRegisteredLocation.distanceTo(location) >= MIN_DISTANCE) {
            updateUserLocation(location)
            if (_uiState.value.isCreatingTrail) updateTrail(location)
            if (!_uiState.value.isCameraDragged) moveCameraToUserLocation() // doesn't move the camera if the camera is being dragged
        }
    }

    private fun handleLocationNotAvailable() {
        setLocationAvailability(false)
    }

    /**
     * Updates the user location state with the given [location].
     */
    private fun updateUserLocation(location: Location) {
        _uiState.update { it.copy(currentUserLocation = location) }
    }

    fun startTrailCreation() {
        _uiState.update { it.copy(isCreatingTrail = true) }
        moveCameraToUserLocation()
    }

    fun stopTrailCreation() {
        _uiState.update {
            it.copy(
                isCreatingTrail = false,
                trailPointsList = emptyList()
            )
        }
    }

    private fun updateTrail(location: Location) {
        val newTrailPoint = TrailPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            timestamp = Date()
        )

        // adds the new location to the list of trail points
        _uiState.update { currentState ->
            currentState.copy(trailPointsList = currentState.trailPointsList + newTrailPoint)
        }
    }

    private fun handleUserLocatingException(exception: Throwable) {
        handleLocationClientException(exception)
    }

    private fun handleTrailCreationException(exception: Throwable) {
        handleLocationClientException(exception)

        if (_uiState.value.isCreatingTrail) {
            _uiState.update { it.copy(isCreatingTrail = false) }
        }
    }

    private fun handleLocationClientException(exception: Throwable) {
        when (exception) {
            is LocationClient.LocationException -> {
                setLocationAvailability(false)
            }

            is LocationClient.LocationNotAvailableException -> {
                setLocationAvailability(false)
            }

            is LocationClient.LocationPermissionNotGrantedException -> {
                setLocationAvailability(false)
            }
        }
    }

    /**
     * Shows a [global][GlobalDriver] status banner with this [type] and [text].
     */
    fun showStatusBanner(type: StatusBannerType, text: Int) {
        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(type, UiText.Resource(text))))
        globalDriver.handleAction(GlobalAction.ShowStatusBanner)
    }

    fun hideStatusBanner() {
        globalDriver.handleAction(GlobalAction.HideStatusBanner)
    }

    /**
     * Sets the location availability state to [isAvailable].
     */
    fun setLocationAvailability(isAvailable: Boolean) {
        _uiState.update { it.copy(isLocationAvailable = isAvailable) }
    }

    /**
     * Sets the camera dragged state to `false` and moves the camera to the user location.
     */
    fun moveCameraToUserLocation() {
        _uiState.update { it.copy(isCameraDragged = false) } // resets the camera dragged state

        _uiState.value.currentUserLocation?.let { userLocation ->
            // updates the camera position on the main thread
            viewModelScope.launch { //Handler(Looper.getMainLooper()).post {}
                _uiState.value.cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(userLocation.toLatLng(), ZOOM_LEVEL)
                )
            }
        }
    }

    /**
     * Sets the camera dragged state to [isDragged].
     */
    fun setCameraDraggedState(isDragged: Boolean) {
        _uiState.update { it.copy(isCameraDragged = isDragged) }
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}