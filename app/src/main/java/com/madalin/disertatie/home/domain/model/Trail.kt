package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Trail(
    val name: String,
    val location: String,
    val length: Int,
    val difficulty: Int,
    val startTime: Date? = null,
    val endTime: Date? = null,
    val trailPointsList: List<TrailPoint> = emptyList(),
    val startingPoint: TrailPoint = trailPointsList.first(),
    val middlePoint: TrailPoint = trailPointsList[trailPointsList.size / 2],
    val endingPoint: TrailPoint = trailPointsList.last(),
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) : Parcelable
