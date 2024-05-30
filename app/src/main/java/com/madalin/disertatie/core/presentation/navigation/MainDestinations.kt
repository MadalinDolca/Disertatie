package com.madalin.disertatie.core.presentation.navigation

/**
 * Main destinations of the app.
 */
sealed class MainDestination(
    val route: String
) {
    data object Login : MainDestination(
        route = "login"
    )

    data object Register : MainDestination(
        route = "register"
    )

    data object PasswordReset : MainDestination(
        route = "password_reset"
    )

    data object Home : MainDestination(
        route = "home"
    )

    data object CameraPreview : MainDestination(
        route = "camera_preview"
    )
}