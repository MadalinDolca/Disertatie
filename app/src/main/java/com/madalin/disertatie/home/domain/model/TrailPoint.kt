package com.madalin.disertatie.home.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class TrailPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val timestamp: Date,
    val note: String? = null,
    val imagesList: List<String> = emptyList(),
    val temperature: Double? = null,
) : Parcelable