package com.madalin.disertatie.core.presentation

import com.madalin.disertatie.core.domain.result.UserFailure
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText

data class GlobalState(
    val isUserLoggedIn: Boolean = false,
    val currentUser: User = User(),

    val userFailureType: UserFailure? = null,

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty)
)