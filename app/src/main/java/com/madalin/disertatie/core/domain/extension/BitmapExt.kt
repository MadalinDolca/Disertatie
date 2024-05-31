package com.madalin.disertatie.core.domain.extension

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

/**
 * Converts this [Bitmap] to a [ByteArray].
 */
fun Bitmap.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}
