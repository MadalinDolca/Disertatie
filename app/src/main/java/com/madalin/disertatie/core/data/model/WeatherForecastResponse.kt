package com.madalin.disertatie.core.data.model

import kotlinx.serialization.Serializable

/**
 * Data class to map the JSON response from the OpenWeatherMap API.
 */
@Serializable
data class WeatherForecastResponse(
    val cod: Int? = null,
    val message: Int? = null,
    val cnt: Int? = null,
    val list: List<WeatherResponse>? = null
)