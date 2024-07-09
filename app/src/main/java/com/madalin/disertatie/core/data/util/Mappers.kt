package com.madalin.disertatie.core.data.util

import com.madalin.disertatie.core.data.model.WeatherForecastResponse
import com.madalin.disertatie.core.data.model.WeatherResponse
import com.madalin.disertatie.core.domain.model.Weather

/**
 * Maps [WeatherResponse] to domain [Weather] model and returns it.
 */
fun WeatherResponse.toWeather() = Weather(
    locationName = this.name,
    weatherMain = this.weather?.get(0)?.main,
    weatherDescription = this.weather?.get(0)?.description,
    weatherIcon = this.weather?.get(0)?.icon,
    mainTemperature = this.main?.temp,
    mainFeelsLike = this.main?.feels_like,
    mainPressure = this.main?.pressure,
    mainHumidity = this.main?.humidity,
    windSpeed = this.wind?.speed,
    clouds = this.clouds?.all,
    datetime = this.dt
)

/**
 * Maps [WeatherForecastResponse] to a list of domain [Weather] model and returns it.
 */
fun WeatherForecastResponse.toWeatherForecast() = this.list?.map { it.toWeather() }.orEmpty()
