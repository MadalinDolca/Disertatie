package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Trail(
    var id: String = generateId(),
    var userId: String,
    var name: String = "",
    var location: String = "",
    var length: Int = -1,
    var difficulty: Int = -1,
    var startTime: Date? = null,
    var endTime: Date? = null,
    var trailPointsList: MutableList<TrailPoint> = mutableListOf(),
    var startingPoint: TrailPoint? = null,
    var middlePoint: TrailPoint? = null,
    var endingPoint: TrailPoint? = null,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable