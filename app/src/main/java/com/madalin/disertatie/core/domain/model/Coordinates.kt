package com.madalin.disertatie.core.domain.model

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coordinates(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Parcelable {
    /**
     * Returns the latitude and longitude of this [Coordinates] as a [Location].
     */
    fun toLocation() = Location("").apply {
        latitude = this@Coordinates.latitude
        longitude = this@Coordinates.longitude
    }

    /**
     * Returns the distance between this [Coordinates] and the given [location] in meters.
     */
    fun distanceTo(location: Location) = this.toLocation().distanceTo(location)
}