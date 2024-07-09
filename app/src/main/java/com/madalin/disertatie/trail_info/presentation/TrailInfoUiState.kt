package com.madalin.disertatie.trail_info.presentation

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.trail_info.domain.model.WeatherTab

data class TrailInfoUiState(
    val currentUser: User = User(),
    val isEditing: Boolean = false,

    // trail info
    val trail: Trail? = null,
    val isLoadingInfo: Boolean = false,
    val loadingInfoError: UiText = UiText.Empty,

    // trail points
    val trailPointsList: List<TrailPoint> = emptyList(),
    val isLoadingPoints: Boolean = false,
    val loadingPointsError: UiText = UiText.Empty,
    val trailPointsDistances: List<Float> = emptyList(),

    // trail images
    val imagesUriList: List<String> = emptyList(),
    val isLoadingImages: Boolean = false,
    val loadingImagesError: UiText = UiText.Empty,

    // weather forecast
    val weatherForecast: List<Weather> = emptyList(),
    val weatherForecastTabs: List<WeatherTab> = emptyList(),
    val isLoadingWeatherForecast: Boolean = false,
    val weatherForecastError: UiText = UiText.Empty
)