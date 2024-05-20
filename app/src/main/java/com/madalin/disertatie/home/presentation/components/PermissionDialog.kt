package com.madalin.disertatie.home.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.LightDarkPreview

/**
 * Dialog used to inform the user about permission states and to request them if not granted.
 * @param permissionTextProvider text to show according to the requested permission and its
 * [isPermanentlyDeclined] state
 * @param isPermanentlyDeclined if the requested permission is permanently declined or not
 * @param onOpenAppSettingsClick function to open the app settings if this permission
 * [is permanently declined][isPermanentlyDeclined]
 * @param onRequestPermissionClick function to request the permission if it's not
 * [permanently declined][isPermanentlyDeclined]
 * @param modifier the [Modifier] to apply
 * @param onDismiss function to call when the user tries to dismiss the dialog by clicking outside
 * or pressing the back button
 */
@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onOpenAppSettingsClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    // in case the user accepts the permission then goes to the app settings to deny it
    // and then resumes the app
    /*val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locationPermissionState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }*/

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.permission_required)) },
        text = { Text(text = permissionTextProvider.getDescription(isPermanentlyDeclined)) },
        confirmButton = {
            Button(onClick = {
                if (isPermanentlyDeclined) onOpenAppSettingsClick()
                else onRequestPermissionClick()
            }) {
                Text(
                    text = if (isPermanentlyDeclined) stringResource(R.string.open_app_settings)
                    else stringResource(R.string.grant_permission)
                )
            }
        }
    )
}

@LightDarkPreview
@Composable
fun PermissionDialogPreview() {
    DisertatieTheme {
        Surface {
            PermissionDialog(
                permissionTextProvider = CoarseLocationPermissionTextProvider(),
                isPermanentlyDeclined = false,
                onOpenAppSettingsClick = {},
                onRequestPermissionClick = {},
                onDismiss = {}
            )
        }
    }
}

interface PermissionTextProvider {
    @Composable
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class CoarseLocationPermissionTextProvider : PermissionTextProvider {
    @Composable
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            val permissionArgument = "Coarse location" //stringResource(R.string.location)
            stringResource(R.string.it_seems_that_you_have_permanently_declined_x_permission, permissionArgument)
        } else {
            stringResource(R.string.coarse_location_usage_explanation)
        }
    }
}

class FineLocationPermissionTextProvider : PermissionTextProvider {
    @Composable
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            val permissionArgument = "Fine location" //stringResource(R.string.location)
            stringResource(R.string.it_seems_that_you_have_permanently_declined_x_permission, permissionArgument)
        } else {
            stringResource(R.string.fine_location_usage_explanation)
        }
    }
}

class CameraPermissionTextProvider : PermissionTextProvider {
    @Composable
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            val permissionArgument = stringResource(R.string.camera).lowercase()
            stringResource(R.string.it_seems_that_you_have_permanently_declined_x_permission, permissionArgument)
        } else {
            stringResource(R.string.camera_usage_explanation)
        }
    }
}
