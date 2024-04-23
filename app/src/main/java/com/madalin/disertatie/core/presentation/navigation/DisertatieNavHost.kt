package com.madalin.disertatie.core.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.madalin.disertatie.auth.presentation.login.LoginScreenRoot
import com.madalin.disertatie.auth.presentation.password_reset.PasswordResetScreen
import com.madalin.disertatie.auth.presentation.register.RegisterScreen
import com.madalin.disertatie.home.presentation.HomeScreen

@Composable
fun DisertatieNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                tween(300)
            )
        }
    ) {
        // login screen
        composable(route = Login.route) {
            LoginScreenRoot(
                onNavigateToRegisterClick = { navController.navigateSingleTopTo(Register.route) },
                onNavigateToPasswordResetClick = { navController.navigateSingleTopTo(PasswordReset.route) }
            )
        }

        // register screen
        composable(route = Register.route) {
            RegisterScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(Login.route) }
            )
        }

        // password reset screen
        composable(route = PasswordReset.route) {
            PasswordResetScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(Login.route) }
            )
        }

        // home screen
        composable(route = Home.route) {
            HomeScreen()
        }
    }
}

/**
 * Navigates to a [route] with SingleTop behaviour.
 *
 * Pops backstack to start destination, preserves destination state and observes SingleTop.
 */
fun NavHostController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}

val NavHostController.canGoBack: Boolean
    get() = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
