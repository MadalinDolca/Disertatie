package com.madalin.disertatie.home.domain.repository

import com.madalin.disertatie.home.domain.state.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double, units: String): Flow<WeatherResult>
}