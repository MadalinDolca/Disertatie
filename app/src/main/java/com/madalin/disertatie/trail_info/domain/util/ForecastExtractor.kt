package com.madalin.disertatie.trail_info.domain.util

import com.madalin.disertatie.core.domain.extension.utcUnixAsDayAndMonth
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.trail_info.domain.model.WeatherTab

/**
 * Returns a list of unique days with their indexes from the given [weatherForecast].
 */
fun getUniqueDaysWithIndexes(weatherForecast: List<Weather>): List<WeatherTab> {
    val uniqueDays = mutableMapOf<String, Int>()

    weatherForecast.forEachIndexed { index, weather ->
        weather.datetime?.let {
            val dayString = it.utcUnixAsDayAndMonth()

            if (!uniqueDays.containsKey(dayString)) {
                uniqueDays[dayString] = index
            }
        }
    }

    return uniqueDays.map { WeatherTab(it.key, it.value) }
}