package com.madalin.disertatie.home.presentation

import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.madalin.disertatie.core.domain.util.isPermanentlyDeclined
import com.madalin.disertatie.home.domain.openAppSettings
import com.madalin.disertatie.home.domain.requestLocationSettings
import com.madalin.disertatie.home.domain.startLocationService
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val applicationContext = LocalContext.current.applicationContext

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
    )

    if (!locationPermissionState.allPermissionsGranted) {
        locationPermissionState.permissions.forEach { permissionState ->
            if (!permissionState.status.isGranted)
                PermissionDialog(
                    permissionTextProvider = when (permissionState.permission) {
                        Manifest.permission.ACCESS_COARSE_LOCATION -> CoarseLocationPermissionTextProvider()
                        Manifest.permission.ACCESS_FINE_LOCATION -> FineLocationPermissionTextProvider()
                        Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                        else -> return@forEach
                    },
                    isPermanentlyDeclined = permissionState.status.isPermanentlyDeclined(),
                    onOpenAppSettingsClick = { context.openAppSettings() },
                    onRequestPermissionClick = { permissionState.launchPermissionRequest() }
                )
        }
    } else {
        Log.d("HomeScreen", "All permissions granted")

        val settingResultRequest = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    startLocationService(applicationContext)
                    Toast.makeText(context, "GPS enabled", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "The location service could not be enabled, please enable it manually", Toast.LENGTH_LONG).show()
                }
            }
        )

        requestLocationSettings(
            context = context,
            onEnabled = {
                startLocationService(applicationContext)
            },
            onDisabled = { intentSenderRequest ->
                settingResultRequest.launch(intentSenderRequest)
            }
        )
    }

    Scaffold(
        bottomBar = {
            Button(onClick = { viewModel.logout() }) {
                Text(text = "logout")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = {
                viewModel.showDialog()
                Log.d("HomeScreen", "Button clicked")
            }) {
                Text(text = "Wtf")
            }
        }
    }
}