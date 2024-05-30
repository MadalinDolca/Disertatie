package com.madalin.disertatie.map.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Converts a vector drawable resource into a [BitmapDescriptor].
 * @param context The context to use for getting the drawable.
 * @param vectorResId The resource ID of the vector drawable.
 * @param scaleFactor A float value that specifies the scale factor to apply to the vector drawable.
 * The default value is `1f`, which means no scaling.
 * @return A [BitmapDescriptor] object representing the scaled vector drawable, or `null` if the
 * vector drawable is not found.
 */
@Composable
fun bitmapDescriptor(context: Context, vectorResId: Int, scaleFactor: Float = 1f): BitmapDescriptor? {
    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    val scaledWidth = (drawable.intrinsicWidth * scaleFactor).toInt()
    val scaledHeight = (drawable.intrinsicHeight * scaleFactor).toInt()

    drawable.setBounds(0, 0, scaledWidth, scaledHeight)
    val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

    // draw it onto the bitmap
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}