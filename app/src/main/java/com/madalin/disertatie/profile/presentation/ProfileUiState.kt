package com.madalin.disertatie.profile.presentation

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.User

data class ProfileUiState(
    val currentUser: User = User(),
    val userTrails: List<Trail> = emptyList(),
)
