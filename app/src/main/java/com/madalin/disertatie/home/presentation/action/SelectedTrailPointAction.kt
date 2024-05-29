package com.madalin.disertatie.home.presentation.action

import android.content.Context
import android.graphics.Bitmap
import com.madalin.disertatie.home.domain.model.TrailImage
import com.madalin.disertatie.home.domain.model.TrailPoint

/**
 * Represents an action that can be performed on a selected [TrailPoint]
 */
sealed class SelectedTrailPointAction {
    data class AddImage(val applicationContext: Context, val image: Bitmap) : SelectedTrailPointAction()
    data class RemoveImage(val trailImage: TrailImage) : SelectedTrailPointAction()
    data class UpdateNote(val note: String) : SelectedTrailPointAction()
    data object GetWeather : SelectedTrailPointAction()
    data object DeleteWeather : SelectedTrailPointAction()
    data class UpdateWarningState(val hasWarning: Boolean) : SelectedTrailPointAction()
    data object ClearData : SelectedTrailPointAction()
}