package com.madalin.disertatie.home.domain

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

/**
 * Checks if location settings are enabled on the device. If disabled, it provides an
 * [IntentSenderRequest] for launching a contract.
 * @param context the activity's [context].
 * @param onEnabled callback to invoke if location settings are enabled
 * @param onDisabled callback to invoke if location settings are disabled along with an
 * [IntentSenderRequest] to start the location settings dialog
 */
fun requestLocationSettings(
    context: Context,
    onEnabled: () -> Unit,
    onDisabled: (IntentSenderRequest) -> Unit
) {
    val locationRequest = LocationRequest.Builder(5000L).build()

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true) // location is required to continue, always show the dialog

    val gpsSettingsTask = LocationServices
        .getSettingsClient(context)
        .checkLocationSettings(builder.build())

    gpsSettingsTask
        .addOnSuccessListener { // invoked if GPS is turned ON
            onEnabled()
        }
        .addOnFailureListener { exception -> // invoked if GPS is turned OFF/disabled
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution)
                        .build()

                    onDisabled(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // ignore here
                }
            }
        }
}

/**
 * Checks if location service is enabled.
 * @return `true` if enabled, `false` otherwise
 */
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

/**
 * Starts a [LocationService].
 */
fun startLocationService(context: Context) {
    Intent(context, LocationService::class.java).apply {
        action = LocationService.ACTION_START
        context.startService(this)
    }
}