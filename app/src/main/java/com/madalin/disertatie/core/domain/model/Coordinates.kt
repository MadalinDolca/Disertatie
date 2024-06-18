package com.madalin.disertatie.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coordinates(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Parcelable