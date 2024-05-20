package com.madalin.disertatie.home.presentation

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraMoveStartedReason
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.home.domain.DefaultLocationClient
import com.madalin.disertatie.home.domain.LocationClient
import com.madalin.disertatie.home.domain.LocationState
import com.madalin.disertatie.home.domain.extensions.hasSameCoordinates
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

    /**
     * Enables location settings. If already enabled, it starts fetching the user's location. If
     * disabled, it launches [activityResultLauncher].
     */
    fun enableLocationSettings(
        applicationContext: Context, activityResultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        requestLocationSettings(context = applicationContext, onEnabled = {
            startLocationFetching(applicationContext)
            setLocationAvailability(true)
        }, onDisabled = { activityResultLauncher.launch(it) })
    }

    fun startLocationFetching(applicationContext: Context) {
        locationFetchingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        locationClient = DefaultLocationClient(
            applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        locationClient.getLocationUpdates(1000L).catch {
            handleUserLocatingException(it)
            handleTrailCreationException(it)
        }.onEach { locationState ->
            when (locationState) {
                is LocationState.LocationData -> handleLocationData(locationState)
                LocationState.LocationAvailable -> handleLocationAvailable()
                LocationState.LocationNotAvailable -> handleLocationNotAvailable()
            }
        }.launchIn(locationFetchingScope)
    }

    fun stopLocationFetching() {
        stopTrailCreation()
        locationFetchingScope.cancel()
    }

    private fun handleLocationData(locationData: LocationState.LocationData) {
        val location = locationData.location
        val lastRegisteredLocation = _uiState.value.currentUserLocation
        val isCreatingTrail = _uiState.value.isCreatingTrail
        val trailPointList = _uiState.value.trailPointsList

        // if no trail point has been registered yet
        // or if the trail creation is started and the list is empty
        // or if the distance between the last registered location and the new location is longer
        // than the minimum distance, then save the new location
        if (lastRegisteredLocation == null
            || (trailPointList.isEmpty() && isCreatingTrail)
            || lastRegisteredLocation.distanceTo(location) >= MIN_DISTANCE
        ) {
            updateUserLocation(location)
            if (isCreatingTrail) updateTrail(location)
            if (!isCameraMovedByGestures()) moveCameraToUserLocation() // doesn't move the camera if the camera has been dragged
        }
    }

    /**
     * Sets the location availability state to `true` only if it was `false` before to avoid
     * unnecessary updates.
     */
    private fun handleLocationAvailable() {
        if (!_uiState.value.isLocationAvailable) {
            setLocationAvailability(true)
        }
    }

    /**
     * Sets the location availability state to `false` only if it was `true` before to avoid
     * unnecessary updates.
     */
    private fun handleLocationNotAvailable() {
        if (_uiState.value.isLocationAvailable) {
            setLocationAvailability(false)
        }
    }

    /**
     * Updates the user location state with the given [location].
     */
    private fun updateUserLocation(location: Location) {
        _uiState.update { it.copy(currentUserLocation = location) }
    }

    /**
     * Sets the location availability state to [isAvailable].
     */
    fun setLocationAvailability(isAvailable: Boolean) {
        _uiState.update { it.copy(isLocationAvailable = isAvailable) }
    }

    fun startTrailCreation() {
        if (!_uiState.value.isLocationAvailable) {
            showStatusBanner(StatusBannerType.Error, R.string.enable_location)
        } else {
            _uiState.update { it.copy(isCreatingTrail = true) }
            moveCameraToUserLocation()
        }
    }

    fun pauseTrailCreation() {
        _uiState.update { it.copy(isCreatingTrailPaused = true) }
    }

    fun resumeTrailCreation() {
        _uiState.update { it.copy(isCreatingTrailPaused = false) }
    }

    fun stopTrailCreation() {
        // TODO stop trail creation and save the data
        _uiState.update {
            it.copy(
                isCreatingTrail = false, trailPointsList = mutableStateListOf()
            )
        }
    }

    private fun updateTrail(location: Location) {
        val newTrailPoint = TrailPoint(
            timestamp = location.time, latitude = location.latitude, longitude = location.longitude, altitude = location.altitude, accuracy = location.accuracy
        )

        // adds the new location to the list of trail points
        _uiState.value.trailPointsList.add(newTrailPoint)
        // TODO update trail points list
        /*_uiState.update { currentState ->
            currentState.copy(trailPointsList = currentState.trailPointsList + newTrailPoint)
        }*/
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
     * Sets the camera dragged state to `false` and moves the camera to the user location.
     */
    fun moveCameraToUserLocation() {
        _uiState.value.currentUserLocation?.let { userLocation ->
            // updates the camera position on the main thread
            viewModelScope.launch { //Handler(Looper.getMainLooper()).post {}
                try {
                    _uiState.value.cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLocation.toLatLng(), ZOOM_LEVEL)
                    )
                } catch (e: Exception) {
                    Log.d("HomeViewModel", "moveCameraToUserLocation: ${e.message}")
                }
            }
        }
    }

    /**
     * Checks if the camera position is positioned on the user's location by checking if their
     * coordinates are the same.
     * @return `true` if positioned, `false` otherwise.
     */
    private fun isCameraPositionedOnUser(): Boolean {
        val currentUserLocation = _uiState.value.currentUserLocation
        val cameraPositionTarget = _uiState.value.cameraPositionState.position.target

        return if (currentUserLocation != null) {
            cameraPositionTarget hasSameCoordinates currentUserLocation.toLatLng()
        } else {
            false
        }
    }

    /**
     * Returns `true` if the camera position has changed due to a user gesture like dragging,
     * otherwise returns `false`.
     */
    private fun isCameraMovedByGestures(): Boolean {
        return _uiState.value.cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
    }

    /**
     * Returns `true` if the camera position has changed due to a developer animation, otherwise
     * returns `false`.
     */
    private fun isCameraMovedByDeveloperAnimation(): Boolean {
        return _uiState.value.cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.DEVELOPER_ANIMATION
    }

    /**
     * Determines if the user location button should be shown. It returns `true` if the camera is not
     * positioned on the user's location and the camera has not been moved by a developer animation.
     */
    fun isUserLocationButtonVisible(): Boolean {
        return !isCameraPositionedOnUser() && !isCameraMovedByDeveloperAnimation()
    }

    /**
     * Shows the trail point info dialog of this [trailPoint] and pauses the trail creation.
     * If the [trailPoint] is `null`, it shows the last registered trail point.
     */
    fun showTrailPointInfoModal(trailPoint: TrailPoint? = null) {
        val trailPointsList = _uiState.value.trailPointsList
        val selectedPoint = trailPoint ?: trailPointsList.lastOrNull()

        if (selectedPoint != null) {
            _uiState.update {
                it.copy(
                    selectedTrailPoint = selectedPoint,
                    isTrailPointInfoModalVisible = true,
                    isCreatingTrailPaused = true
                )
            }
        } else {
            showStatusBanner(StatusBannerType.Error, R.string.no_trail_point_has_been_registered_yet)
        }
    }

    /**
     * Hides the trail point info modal and resumes the trail creation.
     */
    fun hideTrailPointInfoModal() {
        _uiState.update {
            it.copy(
                selectedTrailPoint = null,
                isTrailPointInfoModalVisible = false,
                isCreatingTrailPaused = false
            )
        }
    }

    fun updateTrailPoint(imagesList: List<String>, note: String, hasWarning: Boolean) {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        val selectedTrailPointIndex = _uiState.value.trailPointsList.indexOf(selectedTrailPoint)

        _uiState.value.trailPointsList[selectedTrailPointIndex].apply {
            this.imagesList.addAll(imagesList)
            this.note = note
            this.hasWarning = hasWarning
        }
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}