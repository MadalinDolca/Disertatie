package com.madalin.disertatie.discover.action

import com.madalin.disertatie.core.domain.action.Action

sealed class DiscoverAction : Action {
    data object GetNearbyTrails : DiscoverAction()
    data class Search(val query: String) : DiscoverAction()
}