package com.madalin.disertatie.map.presentation

import android.content.Context
import android.graphics.Bitmap
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
import com.madalin.disertatie.core.domain.SuggestionGenerator
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.model.LocationClassifications
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.domain.result.SuggestionResult
import com.madalin.disertatie.core.domain.util.generateId
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.map.domain.DefaultLocationClient
import com.madalin.disertatie.map.domain.LocationClassifier
import com.madalin.disertatie.map.domain.LocationClient
import com.madalin.disertatie.map.domain.extension.hasSameCoordinates
import com.madalin.disertatie.map.domain.extension.toLatLng
import com.madalin.disertatie.map.domain.repository.WeatherRepository
import com.madalin.disertatie.map.domain.requestLocationSettings
import com.madalin.disertatie.map.domain.result.LocationClassificationResult
import com.madalin.disertatie.map.domain.result.LocationFetchingResult
import com.madalin.disertatie.map.domain.result.WeatherResult
import com.madalin.disertatie.map.presentation.action.SelectedTrailPointAction
import com.madalin.disertatie.map.presentation.action.SuggestionAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class MapViewModel(
    private val globalDriver: GlobalDriver,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val weatherRepository: WeatherRepository,
    private val suggestionGenerator: SuggestionGenerator
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
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
     * Handles the given [action] by calling the appropriate handle method.
     */
    fun handleAction(action: Action) {
        when (action) {
            is SelectedTrailPointAction -> handleSelectedTrailPointAction(action)
            is SuggestionAction -> handleSuggestionAction(action)
        }
    }

    /**
     * Handles the given [SelectedTrailPointAction] by calling the appropriate handle method.
     */
    private fun handleSelectedTrailPointAction(action: SelectedTrailPointAction) {
        when (action) {
            is SelectedTrailPointAction.AddImage -> addAndClassifySTPImage(
                action.applicationContext, action.image
            )

            is SelectedTrailPointAction.RemoveImage -> {
                updateSelectedTrailPoint {
                    val newImagesList = it.imagesList
                    newImagesList.remove(action.trailImage)
                    it.copy(imagesList = newImagesList)
                }
            }

            is SelectedTrailPointAction.UpdateNote -> updateSelectedTrailPoint {
                it.copy(note = action.note)
            }

            SelectedTrailPointAction.GetWeather -> updateSelectedTrailPointWeather()

            SelectedTrailPointAction.DeleteWeather -> updateSelectedTrailPoint {
                it.copy(weather = null)
            }

            is SelectedTrailPointAction.UpdateWarningState -> updateSelectedTrailPoint {
                it.copy(hasWarning = action.hasWarning)
            }

            SelectedTrailPointAction.ClearData -> updateSelectedTrailPoint {
                it.copy(
                    imagesList = mutableListOf(),
                    note = "",
                    weather = null,
                    hasWarning = false
                )
            }
        }
    }

    /**
     * Handles the given [SuggestionAction] by calling the appropriate handle method.
     */
    private fun handleSuggestionAction(action: SuggestionAction) {
        when (action) {
            SuggestionAction.GetActivitySuggestions -> getTrailPointActivitySuggestions()
            is SuggestionAction.SetImageState -> updateSuggestionDialog { it.copy(isImagesChecked = action.isChecked) }
            is SuggestionAction.SetNoteState -> updateSuggestionDialog { it.copy(isNoteChecked = action.isChecked) }
            is SuggestionAction.SetWarningState -> updateSuggestionDialog { it.copy(isWarningChecked = action.isChecked) }
            is SuggestionAction.SetWeatherState -> updateSuggestionDialog { it.copy(isWeatherChecked = action.isChecked) }
            is SuggestionAction.SetTimeState -> updateSuggestionDialog { it.copy(isTimeChecked = action.isChecked) }
            is SuggestionAction.SetAdditionalInfo -> updateSuggestionDialog { it.copy(additionalInfo = action.info) }
            SuggestionAction.CopySuggestion -> copySuggestionToSelectedTrailPoint()
            SuggestionAction.ShowSuggestionDialog -> showSuggestionDialog()
            SuggestionAction.HideSuggestionDialog -> hideSuggestionDialog()
        }
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
     * It handles each [LocationFetchingResult] collection and thrown exceptions.
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
                    is LocationFetchingResult.Data -> handleLocationData(locationState)
                    LocationFetchingResult.Available -> handleLocationAvailable()
                    LocationFetchingResult.NotAvailable -> handleLocationNotAvailable()
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
    private fun handleLocationData(locationData: LocationFetchingResult.Data) {
        val location = locationData.location
        val lastRegisteredLocation = _uiState.value.currentUserLocation
        val isCreatingTrail = _uiState.value.isCreatingTrail

        // if no trail point has been registered yet or if the distance between the last registered
        // location and the new location is longer than the minimum distance, then save the new location
        if (lastRegisteredLocation == null
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
     * Shows a [global][GlobalDriver] status banner with this [type] and [text] message or resource ID.
     */
    fun showStatusBanner(type: StatusBannerType, text: Any) {
        val uiText = when (text) {
            is String -> UiText.Dynamic(text)
            is Int -> UiText.Resource(text)
            else -> UiText.Empty
        }

        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(type, uiText)))
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
                    Log.d("MapViewModel", "moveCameraToUserLocation: ${e.message}")
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
    private fun updateSelectedTrailPoint(update: (TrailPoint) -> TrailPoint) {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        if (selectedTrailPoint == null) {
            Log.e("MapViewModel", "updateSelectedTrailPoint: selectedTrailPoint is null")
            return
        }

        _uiState.update { currentState ->
            var updatedSelectedPoint = selectedTrailPoint.copy()
            updatedSelectedPoint = update(updatedSelectedPoint)
            currentState.copy(selectedTrailPoint = updatedSelectedPoint)
        }
    }

    /**
     * Obtains the weather information for the selected trail point via [weatherRepository] and
     * updates it.
     */
    private fun updateSelectedTrailPointWeather() {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        if (selectedTrailPoint == null) {
            Log.e("MapViewModel", "updateSelectedTrailPointWeather: selectedTrailPoint is null")
            return
        }

        weatherRepository
            .getWeather(selectedTrailPoint.latitude, selectedTrailPoint.longitude, "metric")
            .map { result ->
                when (result) {
                    WeatherResult.Loading -> {
                        _uiState.update { it.copy(isLoadingWeather = true) }
                    }

                    is WeatherResult.Success -> {
                        updateSelectedTrailPoint { it.copy(weather = result.weather) }
                        _uiState.update { it.copy(isLoadingWeather = false) }
                    }

                    is WeatherResult.Error -> {
                        showStatusBanner(StatusBannerType.Error, result.message)
                        _uiState.update { it.copy(isLoadingWeather = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
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

    /**
     * Updates this [trailImage] of the selected trail point with the given [classifications].
     */
    private fun updateSTPImageClassifications(trailImage: TrailImage, classifications: LocationClassifications) {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        if (selectedTrailPoint == null) {
            Log.e("MapViewModel", "updateSelectedTrailPointImage: selectedTrailPoint is null")
            return
        }

        val newImagesList = selectedTrailPoint.imagesList
        newImagesList
            .find { it.id == trailImage.id }
            ?.let { selectedImage ->
                selectedImage.classifications = classifications
                updateSelectedTrailPoint { it.copy(imagesList = newImagesList) }
            }
    }

    private fun addAndClassifySTPImage(applicationContext: Context, image: Bitmap) {
        val classifier = LocationClassifier(applicationContext, image)
        val newImage = TrailImage(image = image)

        updateSelectedTrailPoint {
            val newImagesList = (it.imagesList + newImage).toMutableList()
            it.copy(imagesList = newImagesList)
        }

        classifier
            .classifyImage()
            .map { result ->
                when (result) {
                    LocationClassificationResult.Loading -> {}
                    is LocationClassificationResult.Success -> updateSTPImageClassifications(newImage, result.data)
                    is LocationClassificationResult.Error -> showStatusBanner(StatusBannerType.Error, result.message)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the suggestion dialog state with the result given by the [update] function.
     */
    private fun updateSuggestionDialog(update: (SuggestionDialogState) -> SuggestionDialogState) {
        val suggestionDialogState = _uiState.value.suggestionDialogState

        _uiState.update { currentState ->
            var updatedState = suggestionDialogState.copy()
            updatedState = update(updatedState)
            currentState.copy(suggestionDialogState = updatedState)
        }
    }

    /**
     * Shows the suggestion dialog.
     */
    private fun showSuggestionDialog() {
        _uiState.update { it.copy(isActivitySuggestionsDialogVisible = true) }
    }

    /**
     * Hides the suggestion dialog and clears it's state.
     */
    private fun hideSuggestionDialog() {
        _uiState.update { it.copy(isActivitySuggestionsDialogVisible = false) }
        updateSuggestionDialog { SuggestionDialogState() } // clears the state
    }

    /**
     * Copies the generated suggestion into the note of the selected trail point.
     */
    private fun copySuggestionToSelectedTrailPoint() {
        updateSelectedTrailPoint {
            it.copy(note = it.note + "\n" + _uiState.value.suggestionDialogState.response)
        }
    }

    private fun getTrailPointActivitySuggestions() {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        if (selectedTrailPoint == null) {
            showStatusBanner(StatusBannerType.Error, R.string.no_trail_point_has_been_selected_yet)
            return
        }

        suggestionGenerator.getActivitySuggestions(
            buildPrompt(),
            selectedTrailPoint.extractImages()
        )
            .map { result ->
                when (result) {
                    SuggestionResult.Loading -> setIsSuggestionLoading(true)

                    is SuggestionResult.Success -> {
                        updateSuggestionDialog { it.copy(response = result.response) }
                        setIsSuggestionLoading(false)
                    }

                    is SuggestionResult.Error -> {
                        showStatusBanner(StatusBannerType.Error, UiText.Resource(R.string.error))
                        setIsSuggestionLoading(false)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Builds and returns a prompt to be used in a suggestion generation based on the settings made
     * made in the suggestion dialog and the information of the selected trail point.
     */
    private fun buildPrompt(): String {
        val selectedTrailPoint = _uiState.value.selectedTrailPoint
        val dialogState = _uiState.value.suggestionDialogState

        val imagesText = if (dialogState.isImagesChecked) {
            "is the place in the attached images, "
        } else ""

        val temperatureText = if (dialogState.isWeatherChecked) {
            "the weather is ${selectedTrailPoint?.weather?.weatherDescription}, " +
                    "with a temperature of ${selectedTrailPoint?.weather?.mainTemperature} Degree Celsius, " +
                    "and a wind speed of ${selectedTrailPoint?.weather?.windSpeed} m/s, "
        } else ""

        val trailPointNoteText = if (dialogState.isNoteChecked) {
            "the user said this about this location '${selectedTrailPoint?.note}', "
        } else ""

        // TODO convert time to local time zone
        val timeText = if (dialogState.isTimeChecked) {
            "the time is ${selectedTrailPoint?.timestamp}, "
        } else ""

        val warningText = if (dialogState.isWarningChecked) {
            "it might be a dangerous place, "
        } else ""

        val additionalInfoText = if (dialogState.additionalInfo.isNotEmpty()) {
            "and the user also added this additional info '${dialogState.additionalInfo}'"
        } else ""

        return imagesText + temperatureText + trailPointNoteText + timeText + warningText + additionalInfoText
    }

    private fun setIsSuggestionLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoadingSuggestion = isLoading) }
    }

    fun logout() {
        globalDriver.handleAction(GlobalAction.SetUserLoginStatus(false))
    }
}