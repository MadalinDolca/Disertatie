package com.madalin.disertatie.home.presentation

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraMoveStartedReason
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.actions.GlobalAction
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.domain.util.generateId
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.home.domain.DefaultLocationClient
import com.madalin.disertatie.home.domain.LocationClient
import com.madalin.disertatie.home.domain.LocationState
import com.madalin.disertatie.home.domain.extensions.hasSameCoordinates
import com.madalin.disertatie.home.domain.extensions.toLatLng
import com.madalin.disertatie.home.domain.model.Trail
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
    private val firebaseUserRepository: FirebaseUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var locationFetchingScope: CoroutineScope
    private lateinit var locationClient: LocationClient

    companion object {
        private const val MIN_DISTANCE = 3
        private const val ZOOM_LEVEL = 18f
    }

    override fun onCleared() {
        super.onCleared()
        locationFetchingScope.cancel()
    }

    /**
     * Enables location settings. If already enabled, it starts fetching the user's location. If
     * disabled, it launches [activityResultLauncher].
     */
    fun enableLocationSettings(
        applicationContext: Context,
        activityResultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        requestLocationSettings(
            context = applicationContext,
            onEnabled = {
                startLocationFetching(applicationContext)
                setLocationAvailability(true)
            },
            onDisabled = { activityResultLauncher.launch(it) })
    }

    /**
     * Starts fetching the location using this [applicationContext] for the [LocationClient].
     * It prevents the [LocationClient] from spawning multiple flows if is already getting updates.
     * It handles each [LocationState] collection and thrown exceptions.
     */
    fun startLocationFetching(applicationContext: Context) {
        if (::locationClient.isInitialized && locationClient.isGettingLocationUpdates) return

        locationFetchingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        locationClient = DefaultLocationClient(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))

        locationClient.getLocationUpdates(1000L)
            .catch {
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

    /**
     * Uses this [locationData] to update the user location, the trail and camera position if the
     * conditions are met.
     */
    private fun handleLocationData(locationData: LocationState.LocationData) {
        val location = locationData.location
        val lastRegisteredLocation = _uiState.value.currentUserLocation
        val isCreatingTrail = _uiState.value.isCreatingTrail

        // if no trail point has been registered yet
        // or if the trail creation is started
        // or if the distance between the last registered location and the new location is longer
        // than the minimum distance, then save the new location
        if (lastRegisteredLocation == null
            || isCreatingTrail
            || lastRegisteredLocation.distanceTo(location) >= MIN_DISTANCE
        ) {
            updateUserLocation(location)
            if (isCreatingTrail) addLocationToCurrentTrail(location)
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

    /**
     * If the location is available and the user's current location has been obtained, it creates a
     * new [Trail] with the current user location as the first point of the trail and then updates
     * the state. Calls [moveCameraToUserLocation] to move the camera to the user location.
     */
    fun startTrailCreation() {
        val isLocationAvailable = _uiState.value.isLocationAvailable
        val currentUserLocation = _uiState.value.currentUserLocation

        if (!isLocationAvailable || currentUserLocation == null) {
            showStatusBanner(StatusBannerType.Error, R.string.enable_location)
            return
        }

        // creates the first trail point out of the current user location
        val startTrailPoint = TrailPoint(
            id = generateId(),
            timestamp = currentUserLocation.time,
            latitude = currentUserLocation.latitude,
            longitude = currentUserLocation.longitude,
            altitude = currentUserLocation.altitude,
            accuracy = currentUserLocation.accuracy
        )

        val userId = firebaseUserRepository.getCurrentUserId()
        if (userId == null) {
            showStatusBanner(StatusBannerType.Error, R.string.could_not_get_the_user_id)
            return
        }

        // creates a new trail
        val newTrail = Trail(
            id = generateId(),
            userId = userId,
            startTime = Date(),
            trailPointsList = mutableListOf(startTrailPoint)
        )

        // updates the states and moves the camera to the user location
        _uiState.update {
            it.copy(
                isCreatingTrail = true,
                currentTrail = newTrail
            )
        }

        moveCameraToUserLocation()
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
                isCreatingTrail = false,
                currentTrail = null
            )
        }
    }

    /**
     * Adds the given [location] to the current trail if it exists.
     */
    private fun addLocationToCurrentTrail(location: Location) {
        val currentTrail = _uiState.value.currentTrail

        if (currentTrail == null) {
            showStatusBanner(StatusBannerType.Error, R.string.can_not_add_the_current_location_to_a_non_existent_trail)
            return
        }

        val newTrailPoint = TrailPoint(
            id = generateId(),
            timestamp = location.time,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy
        )

        currentTrail.trailPointsList.add(newTrailPoint)
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
     * Shows the trail point info dialog of this [trailPoint] if the current trail exists and pauses
     * the trail creation. If the [trailPoint] is `null`, it shows the last registered trail point.
     */
    fun showTrailPointInfoModal(trailPoint: TrailPoint? = null) {
        val currentTrail = _uiState.value.currentTrail
        if (currentTrail == null) {
            showStatusBanner(StatusBannerType.Error, R.string.can_not_show_the_trail_point_info_of_a_non_existent_trail)
            return
        }

        val selectedPoint = trailPoint ?: currentTrail.trailPointsList.lastOrNull()
        if (selectedPoint == null) {
            showStatusBanner(StatusBannerType.Error, R.string.no_trail_point_has_been_registered_yet)
            return
        }

        _uiState.update {
            it.copy(
                selectedTrailPoint = selectedPoint.copy(),
                isTrailPointInfoModalVisible = true,
                isCreatingTrailPaused = true
            )
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

    /**
     * Updates the selected trail point with the result given by the [update] function.
     */
    fun updateSelectedTrailPoint(update: (TrailPoint) -> TrailPoint) {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        if (selectedTrailPoint == null) {
            Log.e("HomeViewModel", "updateSelectedTrailPoint: selectedTrailPoint is null")
            return
        }

        _uiState.update { currentState ->
            var updatedSelectedPoint = selectedTrailPoint.copy()
            updatedSelectedPoint = update(updatedSelectedPoint)
            currentState.copy(selectedTrailPoint = updatedSelectedPoint)
        }
    }

    /**
     * Updates the trail point of the current trail and calls [onSuccess] if the update was
     * successful, otherwise calls [onFailure].
     */
    fun updateTrailPoint(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val currentTrail = _uiState.value.currentTrail
        val selectedTrailPoint = _uiState.value.selectedTrailPoint

        if (currentTrail == null || selectedTrailPoint == null) {
            onFailure()
            showStatusBanner(StatusBannerType.Error, R.string.can_not_update_the_trail_point)
            return
        }

        val selectedTrailPointIndex = currentTrail.trailPointsList.indexOfFirst { it.id == selectedTrailPoint.id }
        if (selectedTrailPointIndex == -1) {
            onFailure()
            showStatusBanner(StatusBannerType.Error, R.string.can_not_update_the_trail_point)
            return
        }

        currentTrail.trailPointsList[selectedTrailPointIndex] = selectedTrailPoint.copy()
        onSuccess()
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}