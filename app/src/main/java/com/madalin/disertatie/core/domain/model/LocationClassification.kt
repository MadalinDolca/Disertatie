package com.madalin.disertatie.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the location classification result with the most accurate result as [topResult] and
 * the other results as [otherResults].
 */
@Parcelize
data class LocationClassifications(
    val topResult: LocationClassification,
    val otherResults: List<LocationClassification>
) : Parcelable

/**
 * Represents a location classified as [type] with an accuracy of [accuracy].
 */
@Parcelize
data class LocationClassification(
    val type: LocationType,
    val accuracy: Float
) : Parcelable

/**
 * Represents the type of location.
 */
enum class LocationType {
    BEACH, FOREST, GARDEN, LAKE, MOUNTAIN, PARK
}