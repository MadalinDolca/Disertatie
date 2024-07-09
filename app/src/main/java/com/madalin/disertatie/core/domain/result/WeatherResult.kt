package com.madalin.disertatie.core.domain.result

import com.madalin.disertatie.core.domain.model.Weather

/**
 * Represents the result of a weather request.
 */
sealed class WeatherResult {
    data object Loading : WeatherResult()
    data class Success(val weather: Weather) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
}

/**
 * Represents the result of a weather forecast request.
 */
sealed class WeatherForecastResult {
    data object Loading : WeatherForecastResult()
    data class Success(val weatherForecast: List<Weather>) : WeatherForecastResult()
    data class Error(val message: String?) : WeatherForecastResult()
}