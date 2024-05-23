package com.madalin.disertatie.core.presentation.navigation

interface AppDestination {
    val route: String
}

object Login : AppDestination {
    override val route = "login"
}

object Register : AppDestination {
    override val route = "register"
}

object PasswordReset : AppDestination {
    override val route = "password_reset"
}

object Home : AppDestination {
    override val route = "home"
}

object Camera : AppDestination {
    override val route = "camera"
}