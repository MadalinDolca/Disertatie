package com.madalin.disertatie.core.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailPoint(
    var id: String = generateId(),
    var timestamp: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var accuracy: Float = 0f,
    var note: String = "",
    var imagesList: MutableList<TrailImage> = mutableListOf(),
    var weather: Weather? = null,
    var hasWarning: Boolean = false
) : Parcelable {
    /**
     * Returns the latitude and longitude of this [TrailPoint] as a [LatLng].
     */
    fun toLatLng() = LatLng(this.latitude, this.longitude)

    /**
     * Extracts the images of this trail point and returns it as a list of [Bitmap]s.
     */
    fun extractImages() = this.imagesList.map { it.image }
}