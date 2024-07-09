package com.madalin.disertatie.core.data.network

import com.madalin.disertatie.core.data.model.WeatherForecastResponse
import com.madalin.disertatie.core.data.model.WeatherResponse
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

    @GET("forecast")
    suspend fun getFiveDaysWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): WeatherForecastResponse
}