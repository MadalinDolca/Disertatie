package com.madalin.disertatie.core.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.madalin.disertatie.auth.presentation.login.LoginScreenRoot
import com.madalin.disertatie.auth.presentation.password_reset.PasswordResetScreen
import com.madalin.disertatie.auth.presentation.register.RegisterScreen
import com.madalin.disertatie.camera_preview.presentation.CameraPreviewScreen
import com.madalin.disertatie.home.presentation.HomeScreen

@Composable
fun MainNavHost(
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
        composable(route = MainDestination.Login.route) {
            LoginScreenRoot(
                onNavigateToRegisterClick = { navController.navigateSingleTopTo(MainDestination.Register.route) },
                onNavigateToPasswordResetClick = { navController.navigateSingleTopTo(MainDestination.PasswordReset.route) }
            )
        }

        // register screen
        composable(route = MainDestination.Register.route) {
            RegisterScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(MainDestination.Login.route) }
            )
        }

        // password reset screen
        composable(route = MainDestination.PasswordReset.route) {
            PasswordResetScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(MainDestination.Login.route) }
            )
        }

        // home screen
        composable(route = MainDestination.Home.route) {
            HomeScreen(
                onNavigateToCameraPreview = { navController.navigateSingleTopTo(MainDestination.CameraPreview.route) },
                onGetImageResultOnce = navController::getImageResultOnce
            )
        }

        // camera preview screen
        composable(route = MainDestination.CameraPreview.route) {
            CameraPreviewScreen(
                onGoBackWithImage = navController::goBackWithImage,
                onGoBack = navController::popBackStack
            )
        }
    }
}