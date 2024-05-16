package com.madalin.disertatie.home.domain

import android.location.Location
import com.madalin.disertatie.home.domain.LocationState.LocationData

/**
 * Represents the location state like its [data][LocationData] and availability.
 */
sealed class LocationState {
    data class LocationData(val location: Location) : LocationState()
    data object LocationAvailable : LocationState()
    data object LocationNotAvailable : LocationState()
}