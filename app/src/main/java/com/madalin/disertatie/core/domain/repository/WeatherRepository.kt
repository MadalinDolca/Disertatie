package com.madalin.disertatie.core.domain.repository

import com.madalin.disertatie.core.domain.result.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double, units: String): Flow<WeatherResult>
}