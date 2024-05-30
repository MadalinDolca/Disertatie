package com.madalin.disertatie.map.domain.extension

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Checks if this [permission][PermissionStatus] has been declined twice.
 * @return `true` if permanently declined, `false` otherwise
 */
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionStatus.isPermanentlyDeclined() = !shouldShowRationale && !isGranted