package com.madalin.disertatie.map.domain

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.madalin.disertatie.map.domain.extension.hasLocationPermission
import com.madalin.disertatie.map.domain.extension.isLocationServiceEnabled
import com.madalin.disertatie.map.domain.extension.str
import com.madalin.disertatie.map.domain.result.LocationFetchingResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Default implementation of [LocationClient] that uses [FusedLocationProviderClient].
 */
class DefaultLocationClient(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) : LocationClient {
    override var isGettingLocationUpdates = false

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<com.madalin.disertatie.map.domain.result.LocationFetchingResult> {
        Log.d("DefaultLocationClient", "getLocationUpdates")

        return callbackFlow {
            isGettingLocationUpdates = true

            if (!context.hasLocationPermission()) throw LocationClient.LocationPermissionNotGrantedException()
            if (!context.isLocationServiceEnabled()) throw LocationClient.LocationNotAvailableException()

            // builds a LocationRequest object with the desired update interval
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                interval
            ).build()

            // create a LocationCallback to handle location results
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    // get the last location from the results (if any)
                    result.locations.lastOrNull()?.let { location ->
                        launch {
                            send(LocationFetchingResult.Data(location))
                            Log.d("DefaultLocationClient", "Obtained: ${location.str()}")
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    super.onLocationAvailability(locationAvailability)

                    if (locationAvailability.isLocationAvailable) {
                        launch { send(LocationFetchingResult.Available) }
                    } else { // location not available
                        launch { send(LocationFetchingResult.NotAvailable) }
                    }
                }
            }

            // request location updates from FusedLocationProviderClient
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // request updates on the main thread (not recommended for battery life)
            )

            // cancel location updates when the flow is collected (i.e. when the consumer stops listening)
            awaitClose {
                isGettingLocationUpdates = false
                locationClient.removeLocationUpdates(locationCallback)
                Log.d("DefaultLocationClient", "removeLocationUpdates")
            }
        }
    }
}