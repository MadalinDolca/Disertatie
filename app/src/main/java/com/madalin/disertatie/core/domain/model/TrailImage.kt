package com.madalin.disertatie.core.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailImage(
    val id: String = generateId(),
    var image: Bitmap,
    var classifications: LocationClassifications? = null
) : Parcelable