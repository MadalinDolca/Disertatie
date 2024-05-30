package com.madalin.disertatie.map.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.model.LocationType

/**
 * Returns a string resource based on the [location type][locationType].
 */
@Composable
fun getLocationTypeString(locationType: LocationType): String {
    return stringResource(
        when (locationType) {
            LocationType.BEACH -> R.string.beach
            LocationType.FOREST -> R.string.forest
            LocationType.GARDEN -> R.string.garden
            LocationType.LAKE -> R.string.lake
            LocationType.MOUNTAIN -> R.string.mountain
            LocationType.PARK -> R.string.park
        }
    )
}