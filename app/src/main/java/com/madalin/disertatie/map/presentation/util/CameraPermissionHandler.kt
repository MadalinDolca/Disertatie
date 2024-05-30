@file:OptIn(ExperimentalPermissionsApi::class)

package com.madalin.disertatie.map.presentation.util

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.madalin.disertatie.map.domain.extension.isPermanentlyDeclined
import com.madalin.disertatie.map.domain.extension.openAppSettings
import com.madalin.disertatie.map.presentation.components.CameraPermissionTextProvider
import com.madalin.disertatie.map.presentation.components.PermissionDialog

/**
 * Shows a [PermissionDialog] if the camera permission is not granted, otherwise calls
 * [onPermissionGranted].
 * If a recomposition occurs and the camera permission is still granted then
 * [onPermissionGranted] will not be called again.
 */
@Composable
fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var permissionGrantedHandled by rememberSaveable { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        PermissionDialog(
            permissionTextProvider = CameraPermissionTextProvider(),
            isPermanentlyDeclined = cameraPermissionState.status.isPermanentlyDeclined(),
            onOpenAppSettingsClick = { context.openAppSettings() },
            onRequestPermissionClick = { cameraPermissionState.launchPermissionRequest() },
            onDismiss = { onDismiss() }
        )
        permissionGrantedHandled = false
    } else {
        if (!permissionGrantedHandled) {
            onPermissionGranted()
            permissionGrantedHandled = true
        }
    }
}