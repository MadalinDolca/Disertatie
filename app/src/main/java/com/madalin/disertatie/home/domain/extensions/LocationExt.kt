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

/**
 * Returns true if this [LatLng] has the same coordinates as [other].
 */
infix fun LatLng.hasSameCoordinates(other: LatLng) =
    String.format("%.6f", this.latitude) == String.format("%.6f", other.latitude)
            && String.format("%.6f", this.longitude) == String.format("%.6f", other.longitude)