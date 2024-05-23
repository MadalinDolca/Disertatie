package com.madalin.disertatie.home.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailPoint(
    var id: String,
    var timestamp: Long,
    var latitude: Double,
    var longitude: Double,
    var altitude: Double,
    var accuracy: Float,
    var note: String = "",
    var imagesList: MutableList<Bitmap> = mutableListOf(),
    var temperature: Double? = null,
    var hasWarning: Boolean = false
) : Parcelable