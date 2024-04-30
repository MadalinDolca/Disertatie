package com.madalin.disertatie.home.domain

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Default implementation of [LocationClient] that uses [FusedLocationProviderClient].
 * This class checks for location permissions and enabled providers before requesting updates.
 */
class DefaultLocationClient(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) : LocationClient {
    /**
     * Creates and returns a cold [Flow] that emits the device's location updates with this
     * [interval] in milliseconds.
     * Checks location permission and GPS status before requesting updates.
     *
     * @throws [LocationClient.LocationException]
     * * if location permission is missing
     * * if GPS and Network location providers are both disabled
     */
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if (!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }

            // build a LocationRequest object with the desired update interval
            val locationRequest = LocationRequest.Builder(interval).build()

            // create a LocationCallback to handle location results
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    // get the last location from the results (if any)
                    result.locations.lastOrNull()?.let { location ->
                        launch {
                            send(location)
                            Log.d("DefaultLocationClient", "Current location: ${location.latitude}, ${location.longitude}")
                        }
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
                locationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

/*    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                interval
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    // get the last location from the results (if any)
                    result.locations.lastOrNull()?.let { location ->
                        launch {
                            send(location)
                            Log.d("DefaultLocationClient", "Current location: ${location.latitude}, ${location.longitude}")
                        }
                    }
                }
            }

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true) // location is required to continue, always show the dialog

            val gpsSettingsTask = LocationServices
                .getSettingsClient(context)
                .checkLocationSettings(builder.build())

            gpsSettingsTask
                .addOnSuccessListener { // invoked if GPS is turned ON
                    locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                    Log.d("DefaultLocationClient", "requestLocationUpdates")
                }
                .addOnFailureListener { exception -> // invoked if GPS is turned OFF/disabled
                    val statusCode = (exception as ApiException).statusCode

                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            // location settings need to be enabled, start intent sender
                            // Get the activity and start activity for result (handle in onActivityResult)
                            (exception as ResolvableApiException).startResolutionForResult(context as Activity, 131313)
                            Log.d("DefaultLocationClient", "startResolutionForResult")
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // Location settings are inadequate, notify user
                            Log.e("DefaultLocationClient", "Location settings can't be changed to meet the requirements")
                        }
                    }
                }

            // cancel location updates when the flow is collected (i.e. when the consumer stops listening)
            awaitClose {
                locationClient.removeLocationUpdates(locationCallback)
            }
        }
    }*/
}