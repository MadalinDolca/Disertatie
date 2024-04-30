package com.madalin.disertatie.home.presentation

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@Composable
fun EnableLocationServicesDialog() {
    val context = LocalContext.current

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                Log.d("", "accepted")
            } else {
                Log.d("", "denied")
            }
        }
    )

    Button(onClick = {
        checkLocationSetting(
            context = context,
            onEnabled = {

            },
            onDisabled = { intentSenderRequest ->
                settingResultRequest.launch(intentSenderRequest)
            }
        )
    }) {
        Text(text = "Request permission")
    }
}

fun checkLocationSetting(
    context: Context,
    onEnabled: () -> Unit,
    onDisabled: (IntentSenderRequest) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    builder.setAlwaysShow(true) // location is required to continue
    val gpsSettingTask = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

    gpsSettingTask
        .addOnSuccessListener { response -> // invoked if GPS is turned ON
            val states = response.locationSettingsStates
            states?.isLocationPresent.let { Log.d("Enable Location", "Location settings are satisfied and the device is ready to get the location") }
            onEnabled()
        }
        .addOnFailureListener { exception -> // invoked if GPS is turned OFF/disabled
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution)
                        .build()

                    onDisabled(intentSenderRequest)
                } catch (sendException: IntentSender.SendIntentException) {
                    // ignore here
                }
            }
        }
}