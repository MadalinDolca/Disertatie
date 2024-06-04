package com.madalin.disertatie.core.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.madalin.disertatie.core.domain.util.generateId
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailImage(
    val id: String = generateId(),
    @get:Exclude var image: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
    @get:Exclude var classifications: LocationClassifications? = null
) : Parcelable