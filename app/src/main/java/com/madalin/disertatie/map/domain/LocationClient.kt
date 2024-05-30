package com.madalin.disertatie.map.domain

import com.madalin.disertatie.map.domain.result.LocationFetchingResult
import kotlinx.coroutines.flow.Flow

/**
 * Interface for clients that need to get location updates.
 */
interface LocationClient {
    /**
     * `true` if this [LocationClient] is getting location updates, `false` otherwise.
     */
    var isGettingLocationUpdates: Boolean

    /**
     * Returns a cold [Flow] that emits the [location state][LocationFetchingResult] of the device with this
     * [interval] in milliseconds. If the location is available it emits the
     * [availability][LocationFetchingResult.Available] and the [location data][LocationFetchingResult.Data],
     * otherwise it emits the [unavailability][LocationFetchingResult.NotAvailable].
     * Checks the location permissions and GPS status before requesting updates.
     *
     * @param context
     *
     * @throws [LocationException] if an error occurs while getting the location
     * @throws [LocationPermissionNotGrantedException] if location permission is not granted
     * @throws [LocationNotAvailableException] if GPS is disabled
     */
    fun getLocationUpdates(interval: Long): Flow<LocationFetchingResult>

    /**
     * Exception class for location related errors with a [message].
     */
    class LocationException(message: String) : Exception()

    /**
     * Exception class for location not available.
     */
    class LocationNotAvailableException : Exception()

    /**
     * Exception class for location permission not granted.
     */
    class LocationPermissionNotGrantedException : Exception()
}