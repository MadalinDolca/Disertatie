package com.madalin.disertatie.map.domain

import android.content.Context
import android.content.IntentSender
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