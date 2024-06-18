package com.madalin.disertatie.core.domain.model

import android.graphics.Bitmap
import android.location.Location
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Trail(
    var id: String = generateId(),
    var userId: String = "",
    var name: String = "",
    var description: String = "",
    var public: Boolean = false,
    var duration: Long? = null,
    var length: Float? = null,
    var difficulty: Int? = null,
    var startTime: Date? = null,
    var endTime: Date? = null,
    @get:Exclude var trailPointsList: MutableList<TrailPoint> = mutableListOf(),
    var startingPointCoordinates: Coordinates? = null,
    var middlePointCoordinates: Coordinates? = null,
    var endingPointCoordinates: Coordinates? = null,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    /**
     * Returns all the [Bitmap] images of every [TrailPoint] of this trail.
     */
    fun extractBitmapImages() = trailPointsList.flatMap { it.imagesList }.map { it.image }

    /**
     * Returns all the [TrailImage]s of this trail.
     */
    fun extractTrailImages() = trailPointsList.flatMap { it.imagesList }

    /**
     * Returns the first [TrailPoint] in the trail if the [trailPointsList] is not empty, otherwise
     * returns null.
     */
    fun obtainStartingPoint() = if (trailPointsList.size > 0) trailPointsList.first() else null

    /**
     * Returns the middle [TrailPoint] in the trail if the [trailPointsList] has more than 2
     * elements, otherwise returns null.
     */
    fun obtainMiddlePoint() = if (trailPointsList.size > 2) trailPointsList[trailPointsList.size / 2] else null

    /**
     * Returns the last [TrailPoint] in the trail if the [trailPointsList] had more than 1 element,
     * otherwise returns null.
     */
    fun obtainEndingPoint() = if (trailPointsList.size > 1) trailPointsList.last() else null

    /**
     * Calculates the duration of the trail in milliseconds and returns it.
     *
     * The duration is calculated by subtracting the timestamp of the last and first [TrailPoint]
     * in the trail. If the trail has no starting and ending point, it returns null.
     */
    fun calculateDuration(): Long? {
        val start = obtainStartingPoint()
        val end = obtainEndingPoint()

        return if (start != null && end != null) end.timestamp - start.timestamp
        else null
    }

    /**
     * Calculates the length of the trail in meters and returns it.
     */
    fun calculateLength(): Float {
        var totalDistance = 0f

        for (i in 0 until trailPointsList.size - 1) {
            val currentLocation = Location("")
            currentLocation.latitude = trailPointsList[i].latitude
            currentLocation.longitude = trailPointsList[i].longitude

            val nextLocation = Location("")
            nextLocation.latitude = trailPointsList[i + 1].latitude
            nextLocation.longitude = trailPointsList[i + 1].longitude

            totalDistance += currentLocation.distanceTo(nextLocation)
        }

        return totalDistance
    }
}