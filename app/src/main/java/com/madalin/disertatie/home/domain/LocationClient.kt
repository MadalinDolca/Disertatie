package com.madalin.disertatie.home.domain

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface for clients that need to get location updates.
 */
interface LocationClient {
    /**
     * Returns a cold [Flow] that emits the location of the device with this [interval] in milliseconds.
     * If the location is available it emits the [Location], otherwise it emits `null`.
     * Checks location permission and GPS status before requesting updates.
     *
     * @throws [LocationException] if an error occurs while getting the location
     * @throws [LocationPermissionNotGrantedException] if location permission is not granted
     * @throws [LocationNotAvailableException] if GPS is disabled
     */
    fun getLocationUpdates(interval: Long): Flow<Location?>

    /**
     * Exception class for location related errors with a [message].
     */
    class LocationException(message: String) : Exception()

    class LocationNotAvailableException : Exception()

    class LocationPermissionNotGrantedException : Exception()
}