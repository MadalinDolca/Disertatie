package com.madalin.disertatie.core.presentation

import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.components.StatusBannerData
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.UiText

data class GlobalState(
    // user
    val isUserLoggedIn: Boolean = false,
    val currentUser: User = User(),

    // status banner
    val isStatusBannerVisible: Boolean = false,
    val statusBannerData: StatusBannerData = StatusBannerData(StatusBannerType.Info, UiText.Empty),

    // trails
    val launchedTrailId: String? = null
)