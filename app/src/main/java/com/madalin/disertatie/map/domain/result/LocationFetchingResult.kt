package com.madalin.disertatie.map.domain.result

import android.location.Location
import com.madalin.disertatie.map.domain.result.LocationFetchingResult.Data

/**
 * Represents the location result like its [data][Data] and availability.
 */
sealed class LocationFetchingResult {
    data class Data(val location: Location) : LocationFetchingResult()
    data object Available : LocationFetchingResult()
    data object NotAvailable : LocationFetchingResult()
}