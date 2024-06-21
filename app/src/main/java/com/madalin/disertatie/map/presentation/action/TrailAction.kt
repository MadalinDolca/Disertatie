package com.madalin.disertatie.map.presentation.action

import android.content.Context
import android.graphics.Bitmap
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint

/**
 * Represents an action that can be performed on a trail and its points.
 */
sealed class TrailAction : Action {
    // trail
    data object StartTrailCreation : TrailAction()
    data object StopTrailCreation : TrailAction()
    data class UpdateTrailName(val name: String) : TrailAction()
    data class UpdateTrailDescription(val description: String) : TrailAction()
    data class SetTrailVisibility(val isPublic: Boolean) : TrailAction()
    data object SaveTrail : TrailAction()
    data object DontSaveTrail : TrailAction()

    // trail point
    data class ShowTrailPointInfoModal(val trailPoint: TrailPoint? = null) : TrailAction()
    data object HideTrailPointInfoModal : TrailAction()
    data class UpdateTrailPoint(val onSuccess: () -> Unit, val onFailure: () -> Unit) : TrailAction()

    // selected trail point
    data class AddStpImage(val applicationContext: Context, val image: Bitmap) : TrailAction()
    data class RemoveStpImage(val trailImage: TrailImage) : TrailAction()
    data class UpdateStpNote(val note: String) : TrailAction()
    data object GetStpWeather : TrailAction()
    data object DeleteStpWeather : TrailAction()
    data class UpdateStpWarningState(val hasWarning: Boolean) : TrailAction()
    data object ClearStpData : TrailAction()

    // launched trail
    data object HideLoadingLaunchedTrailDialog : TrailAction()
    data object CloseLaunchedTrail : TrailAction()

    // nearby trails
    data object ShowNearbyTrails : TrailAction()
    data object HideNearbyTrails : TrailAction()
}