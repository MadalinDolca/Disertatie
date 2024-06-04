package com.madalin.disertatie.profile.presentation.action

import com.madalin.disertatie.core.domain.action.Action

sealed class ProfileAction : Action {
    data object DoLogout : ProfileAction()
}