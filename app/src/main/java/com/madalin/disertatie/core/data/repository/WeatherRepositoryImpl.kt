package com.madalin.disertatie.core.data.repository

import com.madalin.disertatie.BuildConfig
import com.madalin.disertatie.core.data.network.WeatherApiService
import com.madalin.disertatie.core.data.util.toWeather
import com.madalin.disertatie.core.data.util.toWeatherForecast
import com.madalin.disertatie.core.domain.repository.WeatherRepository
import com.madalin.disertatie.core.domain.result.WeatherForecastResult
import com.madalin.disertatie.core.domain.result.WeatherResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException

/**
 * Repository to abstract data fetching logic.
 */
class WeatherRepositoryImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {
    private val API_KEY = BuildConfig.WEATHER_API_KEY
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private val contentType = "application/json; charset=UTF8".toMediaType()
    private val json = Json { ignoreUnknownKeys = true }

    private val apiService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApiService::class.java)
    }

    override fun getWeather(lat: Double, lon: Double, units: String) = flow {
        emit(WeatherResult.Loading)

        try {
            val response = apiService.getWeather(lat, lon, API_KEY, units).toWeather()
            emit(WeatherResult.Success(response))
        } catch (e: HttpException) {
            emit(WeatherResult.Error(e.message.orEmpty()))
        } catch (e: IOException) {
            emit(WeatherResult.Error(e.message.orEmpty()))
        } catch (e: Exception) {
            emit(WeatherResult.Error(e.message.orEmpty()))
        }
    }.flowOn(dispatcher)

    override fun getFiveDaysWeatherForecast(lat: Double, lon: Double, units: String) = flow {
        emit(WeatherForecastResult.Loading)

        try {
            val response = apiService.getFiveDaysWeatherForecast(lat, lon, API_KEY, units).toWeatherForecast()
            emit(WeatherForecastResult.Success(response))
        } catch (e: HttpException) {
            emit(WeatherForecastResult.Error(e.message))
        } catch (e: IOException) {
            emit(WeatherForecastResult.Error(e.message))
        } catch (e: Exception) {
            emit(WeatherForecastResult.Error(e.message))
        }
    }.flowOn(dispatcher)
}