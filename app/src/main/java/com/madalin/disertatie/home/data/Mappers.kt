package com.madalin.disertatie.home.data

import com.madalin.disertatie.home.data.model.WeatherResponse
import com.madalin.disertatie.home.domain.model.Weather

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
    clouds = this.clouds?.all
)