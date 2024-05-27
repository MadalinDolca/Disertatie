package com.madalin.disertatie.home.data.network

import com.madalin.disertatie.home.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Weather API endpoints using Retrofit annotations.
 */
interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): WeatherResponse
}