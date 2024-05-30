package com.madalin.disertatie.map.presentation.action

import android.content.Context
import android.graphics.Bitmap
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint

/**
 * Represents an action that can be performed on a selected [TrailPoint]
 */
sealed class SelectedTrailPointAction : Action {
    data class AddImage(val applicationContext: Context, val image: Bitmap) : SelectedTrailPointAction()
    data class RemoveImage(val trailImage: TrailImage) : SelectedTrailPointAction()
    data class UpdateNote(val note: String) : SelectedTrailPointAction()
    data object GetWeather : SelectedTrailPointAction()
    data object DeleteWeather : SelectedTrailPointAction()
    data class UpdateWarningState(val hasWarning: Boolean) : SelectedTrailPointAction()
    data object ClearData : SelectedTrailPointAction()
}