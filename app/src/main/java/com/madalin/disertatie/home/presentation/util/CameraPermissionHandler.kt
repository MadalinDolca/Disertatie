@file:OptIn(ExperimentalPermissionsApi::class)

package com.madalin.disertatie.home.presentation.util

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.madalin.disertatie.core.domain.util.isPermanentlyDeclined
import com.madalin.disertatie.home.domain.extensions.openAppSettings
import com.madalin.disertatie.home.presentation.components.CameraPermissionTextProvider
import com.madalin.disertatie.home.presentation.components.PermissionDialog

/**
 * Shows a [PermissionDialog] if the camera permission is not granted. If granted, calls [onPermissionGranted].
 */
@Composable
fun CameraPermissionHandler(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        PermissionDialog(
            permissionTextProvider = CameraPermissionTextProvider(),
            isPermanentlyDeclined = cameraPermissionState.status.isPermanentlyDeclined(),
            onOpenAppSettingsClick = { context.openAppSettings() },
            onRequestPermissionClick = { cameraPermissionState.launchPermissionRequest() }
        )
    } else {
        onPermissionGranted()
    }
}