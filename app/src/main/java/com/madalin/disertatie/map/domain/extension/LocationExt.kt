package com.madalin.disertatie.map.domain.extension

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.madalin.disertatie.core.domain.model.TrailPoint

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

/**
 * Returns the latitude and longitude of this [TrailPoint] as a [LatLng].
 */
fun TrailPoint.toLatLng() = LatLng(this.latitude, this.longitude)