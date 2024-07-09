package com.madalin.disertatie.trail_info.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.repository.WeatherRepository
import com.madalin.disertatie.core.domain.result.TrailDeleteResult
import com.madalin.disertatie.core.domain.result.TrailImagesResult
import com.madalin.disertatie.core.domain.result.TrailInfoResult
import com.madalin.disertatie.core.domain.result.TrailPointsResult
import com.madalin.disertatie.core.domain.result.TrailUpdateResult
import com.madalin.disertatie.core.domain.result.WeatherForecastResult
import com.madalin.disertatie.core.domain.util.mapTrailPointsAndImageUrls
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.navigation.TrailInfoDest
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.trail_info.domain.util.getUniqueDaysWithIndexes
import com.madalin.disertatie.trail_info.presentation.action.TrailInfoAction
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrailInfoViewModel(
    private val globalDriver: GlobalDriver,
    private val firebaseContentRepository: FirebaseContentRepository,
    private val weatherRepository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrailInfoUiState())
    val uiState = _uiState.asStateFlow()

    // trail ID obtained from the navigation
    private val trailId: String? by lazy { savedStateHandle[TrailInfoDest.idArg] }

    init {
        // collect global state
        viewModelScope.launch {
            globalDriver.state.collect {
                it.reduce()
            }
        }

        // get trail data
        getTrailInfo()
    }

    private fun GlobalState.reduce() {
        _uiState.update { currentState ->
            currentState.copy(
                currentUser = this.currentUser
            )
        }
    }

    /**
     * Handles the given [TrailInfoAction] by calling the appropriate handle method.
     */
    fun handleAction(action: TrailInfoAction) {
        when (action) {
            is TrailInfoAction.SetName -> updateCurrentTrail { it.copy(name = action.name) }
            is TrailInfoAction.SetDescription -> updateCurrentTrail { it.copy(description = action.description) }
            TrailInfoAction.EnableEditing -> _uiState.update { it.copy(isEditing = true) }
            TrailInfoAction.DisableEditing -> _uiState.update { it.copy(isEditing = false) }
            is TrailInfoAction.SetVisibility -> updateCurrentTrail { it.copy(public = action.isPublic) }
            TrailInfoAction.Delete -> deleteTrailFromDatabase()
            TrailInfoAction.Update -> updateTrailInDatabase()
            TrailInfoAction.SetLaunchedTrailId -> setLaunchedTrailId()
        }
    }

    /**
     * Updates the trail with the result given by the [update] function.
     */
    private fun updateCurrentTrail(update: (Trail) -> Trail) {
        val trail = _uiState.value.trail
        if (trail == null) {
            Log.e("TrailInfoViewModel", "updateCurrentTrail: trail is null")
            return
        }

        _uiState.update { it.copy(trail = update(trail)) }
    }

    /**
     * Obtains the info of the trail that has the current [trailId] in the database.
     * Calls [getWeatherForecast] and [getTrailPoints] after the info is obtained.
     */
    private fun getTrailInfo() {
        val id = trailId
        if (id == null) {
            globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Error, R.string.no_trail_id_has_been_provided))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInfo = true) } // loading

            val trailResponse = async { firebaseContentRepository.getTrailInfoById(id) }.await()
            when (trailResponse) {
                is TrailInfoResult.Success -> {
                    _uiState.update {
                        it.copy(
                            trail = trailResponse.trail,
                            isLoadingInfo = false,
                            loadingInfoError = UiText.Empty
                        )
                    }

                    launch {
                        getWeatherForecast(
                            trailResponse.trail.startingPointCoordinates?.latitude,
                            trailResponse.trail.startingPointCoordinates?.longitude
                        )
                    }

                    launch { getTrailPoints() }
                }

                TrailInfoResult.NotFound -> _uiState.update {
                    it.copy(
                        isLoadingInfo = false,
                        loadingInfoError = UiText.Resource(R.string.trail_not_found)
                    )
                }

                is TrailInfoResult.Error -> _uiState.update {
                    it.copy(
                        isLoadingInfo = false,
                        loadingInfoError = UiText.Resource(R.string.could_not_get_trail_info)
                    )
                }
            }
        }
    }

    /**
     * Obtains the trail points of the current trail from the database.
     * Calls [setTrailPointsDistances] and [getTrailImages] after the points are obtained.
     */
    private fun getTrailPoints() {
        val id = trailId
        if (id == null) {
            globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Error, R.string.no_trail_id_has_been_provided))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPoints = true) } // loading

            val response = async { firebaseContentRepository.getTrailPointsByTrailId(id) }.await()
            when (response) {
                is TrailPointsResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            trailPointsList = response.points,
                            isLoadingPoints = false,
                            loadingPointsError = UiText.Empty
                        )
                    }
                    setTrailPointsDistances()
                    getTrailImages()
                }

                is TrailPointsResult.Error -> _uiState.update {
                    it.copy(
                        isLoadingPoints = false,
                        loadingPointsError = if (response.error != null) UiText.Dynamic(response.error)
                        else UiText.Resource(R.string.could_not_get_trail_points)
                    )
                }
            }
        }
    }

    /**
     * Obtains the trail images of the current trail from the database.
     * After the images are obtained, it maps the trail points with the images using [mapTrailPointsAndImageUrls].
     */
    private fun getTrailImages() {
        val id = trailId
        if (id == null) {
            globalDriver.onAction(GlobalAction.ShowStatusBanner(StatusBannerType.Error, R.string.no_trail_id_has_been_provided))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingImages = true) } // loading

            val response = async { firebaseContentRepository.getTrailImagesByTrailId(id) }.await()
            when (response) {
                is TrailImagesResult.Success -> {
                    _uiState.update {
                        it.copy(
                            imagesUriList = response.images,
                            isLoadingImages = false,
                            loadingInfoError = UiText.Empty
                        )
                    }
                    mapTrailPointsAndImageUrls(_uiState.value.trailPointsList, _uiState.value.imagesUriList)
                }

                is TrailImagesResult.Error -> _uiState.update {
                    it.copy(
                        isLoadingImages = false,
                        loadingImagesError = if (response.error != null) UiText.Dynamic(response.error)
                        else UiText.Resource(R.string.could_not_get_trail_images)
                    )
                }
            }
        }
    }

    private fun getWeatherForecast(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            _uiState.update { it.copy(weatherForecastError = UiText.Resource(R.string.could_not_get_the_weather_forecast_because_the_coordinates_are_null)) }
            return
        }

        weatherRepository
            .getFiveDaysWeatherForecast(latitude, longitude, "metric")
            .map { result ->
                when (result) {
                    WeatherForecastResult.Loading -> {
                        _uiState.update { it.copy(isLoadingWeatherForecast = true) }
                    }

                    is WeatherForecastResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingWeatherForecast = false,
                                weatherForecast = result.weatherForecast,
                                weatherForecastTabs = getUniqueDaysWithIndexes(result.weatherForecast),
                                weatherForecastError = UiText.Empty
                            )
                        }
                    }

                    is WeatherForecastResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingWeatherForecast = false,
                                weatherForecast = emptyList(),
                                weatherForecastTabs = emptyList(),
                                weatherForecastError = if (result.message != null) {
                                    UiText.Dynamic(result.message)
                                } else {
                                    UiText.Resource(R.string.could_not_get_weather_forecast)
                                }
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Updates the trail with the current values in the database.
     */
    private fun updateTrailInDatabase() {
        val id = trailId
        val trail = _uiState.value.trail
        if (id == null || trail == null) {
            Log.e("TrailInfoViewModel", "updateTrailInDatabase: trailId or trail is null")
            return
        }

        viewModelScope.launch {
            val newData = mapOf(
                "name" to trail.name,
                "description" to trail.description,
                "public" to trail.public
            )
            val result = async { firebaseContentRepository.updateTrailById(id, newData) }.await()

            when (result) {
                TrailUpdateResult.Success -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Success, R.string.trail_has_been_updated)
                )

                is TrailUpdateResult.Error -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_update_trail)
                )
            }
        }
    }

    /**
     * Deletes the current trail from the database.
     */
    private fun deleteTrailFromDatabase() {
        val id = trailId
        if (id == null) {
            Log.e("TrailInfoViewModel", "deleteTrailFromDatabase: trailId is null")
            return
        }

        viewModelScope.launch {
            val result = async { firebaseContentRepository.deleteTrailById(id) }.await()

            when (result) {
                TrailDeleteResult.Success -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Success, R.string.trail_has_been_deleted)
                )

                is TrailDeleteResult.Error -> globalDriver.onAction(
                    GlobalAction.ShowStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_delete_trail)
                )
            }
        }
    }

    /**
     * Sets the [global][GlobalDriver] launched trail ID to this [trailId].
     */
    private fun setLaunchedTrailId() {
        val id = trailId
        if (id == null) {
            Log.e("TrailInfoViewModel", "setLaunchedTrailId: trailId is null")
            return
        }

        globalDriver.onAction(GlobalAction.SetLaunchedTrailId(id))
    }

    /**
     * Sets the trail points distances in the state.
     */
    private fun setTrailPointsDistances() {
        val trailPoints = _uiState.value.trailPointsList
        val distances = Trail(trailPointsList = trailPoints.toMutableList()).calculateTrailPointsDistances()

        _uiState.update { it.copy(trailPointsDistances = distances) }
    }
}