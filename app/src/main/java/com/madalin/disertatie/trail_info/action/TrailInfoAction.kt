package com.madalin.disertatie.trail_info.action

import com.madalin.disertatie.core.domain.action.Action

/**
 * Actions that can be performed on a trail.
 */
sealed class TrailInfoAction : Action {
    data class SetName(val name: String) : TrailInfoAction()
    data class SetDescription(val description: String) : TrailInfoAction()
    data class SetVisibility(val isPublic: Boolean) : TrailInfoAction()
    data object Delete : TrailInfoAction()
    data object Update : TrailInfoAction()
    data object SetLaunchedTrailId : TrailInfoAction()
}