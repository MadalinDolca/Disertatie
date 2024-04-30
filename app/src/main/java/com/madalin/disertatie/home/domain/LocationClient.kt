package com.madalin.disertatie.home.domain

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface for clients that need to get location updates.
 */
interface LocationClient {
    /**
     * Starts a stream of location updates with the specified [interval] in milliseconds.
     *
     * @throws LocationException if there's an issue getting location updates,
     *  like missing permissions or disabled location providers.
     * @return a flow that emits the device's location updates
     */
    fun getLocationUpdates(interval: Long): Flow<Location>

    /**
     * Exception class for location related errors (i.e. If the app doesn't have location permission
     * or if the GPS is disabled).
     */
    class LocationException(message: String) : Exception()
}