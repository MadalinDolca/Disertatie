package com.madalin.disertatie.home.presentation.components

import android.location.Location
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
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.extensions.toLatLng
import com.madalin.disertatie.home.presentation.util.bitmapDescriptor

/**
 * Marker used to indicate the [user location][userLocation] on the map.
 */
@Composable
fun UserMarker(userLocation: Location) {
    MarkerInfoWindow(
        state = MarkerState(position = userLocation.toLatLng()),
        icon = bitmapDescriptor(
            context = LocalContext.current,
            vectorResId = R.drawable.user_pin
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