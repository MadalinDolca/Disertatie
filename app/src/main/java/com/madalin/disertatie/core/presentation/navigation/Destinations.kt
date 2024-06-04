package com.madalin.disertatie.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.UiText

interface Destination {
    val route: String
}

interface HomeDestination : Destination {
    val title: UiText
    val icon: ImageVector
}

object LoginDest : Destination {
    override val route = "login"
}

object RegisterDest : Destination {
    override val route = "register"
}

object PasswordResetDest : Destination {
    override val route = "password_reset"
}

object HomeDest : Destination {
    const val trailIdArg = "trailId"

    override val route = "home?trailId={$trailIdArg}"

    val arguments = listOf(navArgument(trailIdArg) {
        nullable = true
        type = NavType.StringType
    })

    fun routeBuilder(trailId: String) = "home?trailId=$trailId"
}

object CameraPreviewDest : Destination {
    override val route = "camera_preview"
}

object TrailInfoDest : Destination {
    const val idArg = "id"
    override val route = "trail_info/{$idArg}"

    val arguments = listOf(navArgument(idArg) {
        type = NavType.StringType
    })

    fun routeBuilder(id: String) = "trail_info/$id"
}

object DiscoverDest : HomeDestination {
    override val route = "discover"
    override val title = UiText.Resource(R.string.discover)
    override val icon = Icons.Rounded.Explore
}

object MapDest : HomeDestination {
    const val trailIdArg = "trailId"

    override val route = "map?trailId={$trailIdArg}"
    override val title = UiText.Resource(R.string.map)
    override val icon = Icons.Rounded.Map

    val arguments = listOf(navArgument(trailIdArg) {
        nullable = true
        type = NavType.StringType
    })

    fun routeBuilder(trailId: String) = "map?trailId=$trailId"
}

object ProfileDest : HomeDestination {
    override val route = "profile"
    override val title = UiText.Resource(R.string.profile)
    override val icon = Icons.Rounded.AccountCircle
}