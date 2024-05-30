package com.madalin.disertatie.map.domain.repository

import com.madalin.disertatie.map.domain.result.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double, units: String): Flow<WeatherResult>
}