package com.madalin.disertatie.home.domain.result

import com.madalin.disertatie.home.domain.model.Weather

/**
 * Represents the result of a weather request.
 */
sealed class WeatherResult {
    data object Loading : WeatherResult()
    data class Success(val weather: Weather) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
}