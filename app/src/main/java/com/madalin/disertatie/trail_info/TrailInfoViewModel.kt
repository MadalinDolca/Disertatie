package com.madalin.disertatie.trail_info

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.GlobalAction
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.result.TrailDeleteResult
import com.madalin.disertatie.core.domain.result.TrailImagesResult
import com.madalin.disertatie.core.domain.result.TrailInfoResult
import com.madalin.disertatie.core.domain.result.TrailPointsResult
import com.madalin.disertatie.core.domain.result.TrailUpdateResult
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.GlobalState
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.navigation.TrailInfoDest
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.trail_info.action.TrailInfoAction
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrailInfoViewModel(
    private val globalDriver: GlobalDriver,
    private val firebaseContentRepository: FirebaseContentRepository,
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

        // get trail info
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
     * Calls [getTrailPoints] and [getTrailImages] after the info is obtained.
     */
    private fun getTrailInfo() {
        val id = trailId
        if (id == null) {
            Log.e("TrailInfoViewModel", "getTrailInfo: trailId is null")
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
                    getTrailPoints()
                    getTrailImages()
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
     */
    private fun getTrailPoints() {
        val id = trailId
        if (id == null) {
            Log.e("TrailInfoViewModel", "getTrailPoints: trailId is null")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPoints = true) } // loading
            val response = async { firebaseContentRepository.getTrailPointsByTrailId(id) }.await()

            when (response) {
                is TrailPointsResult.Success -> _uiState.update { currentState ->
                    updateCurrentTrail { it.copy(trailPointsList = response.points.toMutableList()) }
                    currentState.copy(
                        isLoadingPoints = false,
                        loadingPointsError = UiText.Empty
                    )
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
     */
    private fun getTrailImages() {
        val id = trailId
        if (id == null) {
            Log.e("TrailInfoViewModel", "getTrailImages: trailId is null")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingImages = true) } // loading
            val response = async { firebaseContentRepository.getTrailImagesByTrailId(id) }.await()

            when (response) {
                is TrailImagesResult.Success -> _uiState.update {
                    it.copy(
                        imagesUriList = response.images,
                        isLoadingImages = false,
                        loadingInfoError = UiText.Empty
                    )
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
                TrailUpdateResult.Success -> showStatusBanner(StatusBannerType.Success, R.string.trail_has_been_updated)
                is TrailUpdateResult.Error -> showStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_update_trail)
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
                TrailDeleteResult.Success -> showStatusBanner(StatusBannerType.Success, R.string.trail_has_been_deleted)
                is TrailDeleteResult.Error -> showStatusBanner(StatusBannerType.Error, result.error ?: R.string.could_not_delete_trail)
            }
        }
    }

    /**
     * Shows a [global][GlobalDriver] status banner with this [type] and [text] message or resource ID.
     */
    private fun showStatusBanner(type: StatusBannerType, text: Any) {
        val uiText = when (text) {
            is String -> UiText.Dynamic(text)
            is Int -> UiText.Resource(text)
            else -> UiText.Empty
        }

        globalDriver.handleAction(GlobalAction.SetStatusBannerData(StatusBannerData(type, uiText)))
        globalDriver.handleAction(GlobalAction.ShowStatusBanner)
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

        globalDriver.handleAction(GlobalAction.SetLaunchedTrailId(id))
    }
}