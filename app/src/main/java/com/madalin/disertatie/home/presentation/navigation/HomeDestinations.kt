package com.madalin.disertatie.home.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.ui.graphics.vector.ImageVector
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.UiText

/**
 * Destinations used inside the home screen.
 */
sealed class HomeDestination(
    val route: String,
    val title: UiText,
    val icon: ImageVector
) {
    data object Discover : HomeDestination(
        route = "discover",
        title = UiText.Resource(R.string.discover),
        icon = Icons.Rounded.Explore
    )

    data object Map : HomeDestination(
        route = "map",
        title = UiText.Resource(R.string.map),
        icon = Icons.Rounded.Map
    )

    data object Profile : HomeDestination(
        route = "profile",
        title = UiText.Resource(R.string.profile),
        icon = Icons.Rounded.AccountCircle
    )
}