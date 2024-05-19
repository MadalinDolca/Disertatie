package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    var note: String? = null,
    val imagesList: MutableList<String> = mutableListOf(),
    val temperature: Double? = null,
) : Parcelable