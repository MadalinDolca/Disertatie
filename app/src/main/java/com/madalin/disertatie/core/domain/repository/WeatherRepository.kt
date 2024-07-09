package com.madalin.disertatie.core.domain.repository

import com.madalin.disertatie.core.domain.result.WeatherForecastResult
import com.madalin.disertatie.core.domain.result.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    /**
     * Obtains the current weather of the location at the given [lat] and [lon] using these [units]
     * and returns it as a [WeatherResult].
     */
    fun getWeather(
        lat: Double, lon: Double,
        units: String
    ): Flow<WeatherResult>

    /**
     * Obtains the five days with 3-hour step weather forecast of the location at the given [lat]
     * and [lon] using these [units] and returns it as a [WeatherForecastResult].
     */
    fun getFiveDaysWeatherForecast(
        lat: Double, lon: Double,
        units: String
    ): Flow<WeatherForecastResult>
}