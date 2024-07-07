package com.madalin.disertatie.discover.presentation.action

import com.madalin.disertatie.core.domain.action.Action

sealed class DiscoverAction : Action {
    data object RefreshNearbyTrails : DiscoverAction()
    data object RefreshLimitedTrails : DiscoverAction()
    data class Search(val query: String) : DiscoverAction()
}