package com.madalin.disertatie.home.domain.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Returns the latitude and longitude of this [Location] as a [LatLng].
 */
fun Location.toLatLng() = LatLng(this.latitude, this.longitude)

/**
 * Returns a string containing the latitude and longitude of this [Location].
 */
fun Location.str() = "$latitude, $longitude"