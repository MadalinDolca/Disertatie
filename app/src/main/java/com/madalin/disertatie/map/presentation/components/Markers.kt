package com.madalin.disertatie.map.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.presentation.util.bitmapDescriptor

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
 * Indicated whether this is the starting or the ending point of a trail.
 */
enum class ExtremePointType {
    START, END
}

/**
 * Marker used to indicate the starting or the ending point of a trail on the map alongside with
 * [trail] information if provided.
 */
@Composable
fun TrailExtremePointMarker(
    type: ExtremePointType,
    coordinates: LatLng,
    trail: Trail?
) {
    val icon = bitmapDescriptor(
        context = LocalContext.current,
        vectorResId = when (type) {
            ExtremePointType.START -> R.drawable.dot
            ExtremePointType.END -> R.drawable.finish_flag
        }
    )

    val title = when (type) {
        ExtremePointType.START -> stringResource(R.string.starting_point)
        ExtremePointType.END -> stringResource(R.string.ending_point)
    }

    MarkerInfoWindow(
        state = MarkerState(position = coordinates),
        icon = icon
    ) {
        Card(modifier = Modifier.padding(bottom = Dimens.separator)) {
            Column(
                modifier = Modifier.padding(Dimens.container),
                verticalArrangement = Arrangement.spacedBy(Dimens.separator, Alignment.CenterVertically)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                if (trail != null) {
                    Text(
                        text = trail.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(Dimens.separator))

                    Text(
                        text = trail.description,
                        maxLines = 5
                    )
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