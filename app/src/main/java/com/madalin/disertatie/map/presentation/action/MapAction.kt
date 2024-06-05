package com.madalin.disertatie.map.presentation.action

import com.madalin.disertatie.core.domain.action.Action

/**
 * Represents an action that can be performed on the Map UI.
 */
sealed class MapAction : Action {
    data object MoveCameraToUserLocation : MapAction()
    data object MoveCameraToLaunchedTrailStartingPoint : MapAction()
    data object MoveCameraToLaunchedTrailEndingPoint : MapAction()
}