package com.madalin.disertatie.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.extensions.toLatLng
import com.madalin.disertatie.home.domain.model.Trail
import com.madalin.disertatie.home.domain.model.TrailPoint
import com.madalin.disertatie.home.presentation.util.bitmapDescriptor

/**
 * Marker used to indicate the [user location][userLocation] on the map.
 */
@Composable
fun UserMarker(coordinates: LatLng) {
    MarkerInfoWindow(
        state = MarkerState(position = coordinates),
        icon = bitmapDescriptor(
            context = LocalContext.current,
            vectorResId = R.drawable.explorer
        )
    ) {
        Card(modifier = Modifier.padding(bottom = Dimens.separator)) {
            Column(
                modifier = Modifier.padding(Dimens.container),
                verticalArrangement = Arrangement.spacedBy(Dimens.separator, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.girl_holding_map),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Text(text = stringResource(R.string.you_are_here))
            }
        }
    }
}

/**
 * Marker used to indicate the starting point of a trail on the map alongside with [trail] information
 * if provided.
 */
@Composable
fun TrailStartMarker(
    coordinates: LatLng,
    trail: Trail? = null
) {
    MarkerInfoWindow(
        state = MarkerState(position = coordinates),
        icon = bitmapDescriptor(
            context = LocalContext.current,
            vectorResId = R.drawable.dot
        )
    ) {
        Card(modifier = Modifier.padding(bottom = Dimens.separator)) {
            Column(
                modifier = Modifier.padding(Dimens.container),
                verticalArrangement = Arrangement.spacedBy(Dimens.separator, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.this_is_the_starting_point))

                if (trail != null) {
                    Text(text = stringResource(R.string.trail_name) + ": " + trail.name)
                    Text(text = stringResource(R.string.length) + ": " + trail.length)
                }
            }
        }
    }
}

@Composable
fun TrailPointInfoMarker(
    trailPoint: TrailPoint,
    onClick: () -> Unit
) {
    val iconRes = if (trailPoint.hasWarning) R.drawable.warning_sign else R.drawable.note

    Marker(
        state = MarkerState(position = trailPoint.toLatLng()),
        icon = bitmapDescriptor(
            context = LocalContext.current,
            vectorResId = iconRes
        ),
        onClick = {
            onClick()
            false
        }
    )
}