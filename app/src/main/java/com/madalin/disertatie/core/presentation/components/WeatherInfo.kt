package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.asDate
import com.madalin.disertatie.core.domain.extension.utcUnixAsDateAndTime
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.core.presentation.util.Dimens

enum class WeatherInfoMode {
    SINGLE, FORECAST
}

@Composable
fun WeatherInfo(
    weather: Weather,
    mode: WeatherInfoMode,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                SubcomposeAsyncImage(
                    model = "https://openweathermap.org/img/wn/${weather.weatherIcon}@2x.png",
                    loading = { CircularProgressIndicator() },
                    contentDescription = "Weather icon",
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${weather.weatherMain}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${weather.weatherDescription}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(Dimens.separator))

            Column {
                Text(
                    text = when (mode) {
                        WeatherInfoMode.SINGLE -> "${weather.locationName}"
                        WeatherInfoMode.FORECAST -> "${weather.datetime?.utcUnixAsDateAndTime()}"
                    }
                )
                Text(
                    text = "${weather.mainTemperature} " + stringResource(R.string.unit_celsius_degrees),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.feels_like_x_celsius_degrees, "${weather.mainFeelsLike}"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Row {
                    Icon(imageVector = Icons.Rounded.WindPower, contentDescription = "Wind speed")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.windSpeed} " + stringResource(R.string.unit_meters_per_second))
                }
                Row {
                    Icon(imageVector = Icons.Rounded.Cloud, contentDescription = "Clouds")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.clouds} " + stringResource(R.string.unit_percent))
                }
            }
            Spacer(modifier = Modifier.width(Dimens.separator * 2))

            Column {
                Row {
                    Icon(imageVector = Icons.Rounded.Compress, contentDescription = "Pressure")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.mainPressure} " + stringResource(R.string.unit_atmospheric_pressure_on_the_sea_level))
                }
                Row {
                    Icon(imageVector = Icons.Rounded.WaterDrop, contentDescription = "Humidity")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.mainHumidity} " + stringResource(R.string.unit_percent))
                }
            }
        }
    }
}