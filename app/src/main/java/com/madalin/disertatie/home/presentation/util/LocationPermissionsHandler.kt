@file:OptIn(ExperimentalPermissionsApi::class)

package com.madalin.disertatie.home.presentation.util

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.madalin.disertatie.core.domain.util.isPermanentlyDeclined
import com.madalin.disertatie.home.domain.extensions.openAppSettings
import com.madalin.disertatie.home.presentation.components.CoarseLocationPermissionTextProvider
import com.madalin.disertatie.home.presentation.components.FineLocationPermissionTextProvider
import com.madalin.disertatie.home.presentation.components.PermissionDialog

/**
 * Shows a [PermissionDialog] if the location permissions are not granted. If granted, calls
 * [onPermissionGranted].
 */
@Composable
fun LocationPermissionsHandler(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    if (!locationPermissionState.allPermissionsGranted) {
        locationPermissionState.permissions.forEach { permissionState ->
            if (!permissionState.status.isGranted)
                PermissionDialog(
                    permissionTextProvider = when (permissionState.permission) {
                        Manifest.permission.ACCESS_COARSE_LOCATION -> CoarseLocationPermissionTextProvider()
                        Manifest.permission.ACCESS_FINE_LOCATION -> FineLocationPermissionTextProvider()
                        else -> return@forEach
                    },
                    isPermanentlyDeclined = permissionState.status.isPermanentlyDeclined(),
                    onOpenAppSettingsClick = { context.openAppSettings() },
                    onRequestPermissionClick = { permissionState.launchPermissionRequest() }
                )
        }
    } else {
        onPermissionGranted()
    }
}