package com.madalin.disertatie.trail_info

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.presentation.util.UiText

data class TrailInfoUiState(
    val currentUser: User = User(),
    val trail: Trail? = null,

    // trail info
    val isLoadingInfo: Boolean = false,
    val loadingInfoError: UiText = UiText.Empty,

    // trail points
    val isLoadingPoints: Boolean = false,
    val loadingPointsError: UiText = UiText.Empty,

    // trail images
    val imagesUriList: List<String> = emptyList(),
    val isLoadingImages: Boolean = false,
    val loadingImagesError: UiText = UiText.Empty
)