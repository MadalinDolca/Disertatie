package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weather(
    val locationName: String? = null,
    val weatherMain: String? = null,
    val weatherDescription: String? = null,
    val weatherIcon: String? = null,
    val mainTemperature: Double? = null,
    val mainFeelsLike: Double? = null,
    val mainPressure: Int? = null,
    val mainHumidity: Int? = null,
    val windSpeed: Double? = null,
    val clouds: Int? = null
) : Parcelable