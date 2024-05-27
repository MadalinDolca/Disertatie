package com.madalin.disertatie.home.data.model

import kotlinx.serialization.Serializable

/**
 * Data classes to map the JSON response from the OpenWeatherMap API.
 */
@Serializable
data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>? = null,
    val base: String? = null,
    val main: Main? = null,
    val visibility: Int? = null,
    val wind: Wind? = null,
    val rain: Rain? = null,
    val clouds: Clouds? = null,
    val dt: Long? = null,
    val sys: Sys? = null,
    val timezone: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val cod: Int? = null
)

@Serializable
data class Coord(
    val lon: Double? = null,
    val lat: Double? = null
)

@Serializable
data class Weather(
    val id: Int? = null,
    val main: String? = null,
    val description: String? = null,
    val icon: String? = null
)

@Serializable
data class Main(
    val temp: Double? = null,
    val feels_like: Double? = null,
    val temp_min: Double? = null,
    val temp_max: Double? = null,
    val pressure: Int? = null,
    val humidity: Int? = null,
    val sea_level: Int? = null,
    val grnd_level: Int? = null
)

@Serializable
data class Wind(
    val speed: Double? = null,
    val deg: Int? = null,
    val gust: Double? = null
)

@Serializable
data class Rain(
    val h: Double? = null
) // Assuming "h" represents rain in the last hour

@Serializable
data class Clouds(
    val all: Int? = null
)

@Serializable
data class Sys(
    val type: Int? = null,
    val id: Int? = null,
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null
)

