package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailPoint(
    var id: String = generateId(),
    var timestamp: Long,
    var latitude: Double,
    var longitude: Double,
    var altitude: Double,
    var accuracy: Float,
    var note: String = "",
    var imagesList: MutableList<TrailImage> = mutableListOf(),
    var weather: Weather? = null,
    var hasWarning: Boolean = false
) : Parcelable