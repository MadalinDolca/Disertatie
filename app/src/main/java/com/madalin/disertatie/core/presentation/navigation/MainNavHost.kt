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
import com.madalin.disertatie.trail_info.presentation.TrailInfoScreen

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
        composable(route = LoginDest.route) {
            LoginScreenRoot(
                onNavigateToRegisterClick = { navController.navigateSingleTopTo(RegisterDest.route) },
                onNavigateToPasswordResetClick = { navController.navigateSingleTopTo(PasswordResetDest.route) }
            )
        }

        // register screen
        composable(route = RegisterDest.route) {
            RegisterScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(LoginDest.route) }
            )
        }

        // password reset screen
        composable(route = PasswordResetDest.route) {
            PasswordResetScreen(
                onNavigateToLoginClick = { navController.navigateSingleTopTo(LoginDest.route) }
            )
        }

        // home screen
        composable(route = HomeDest.route) {
            HomeScreen(
                onNavigateToCameraPreview = { navController.navigateSingleTopTo(CameraPreviewDest.route) },
                onGetImageResultOnce = navController::getImageResultOnce,
                onNavigateToTrailInfoWithTrailId = navController::navigateToTrailInfoWithTrailId
            )
        }

        // camera preview screen
        composable(route = CameraPreviewDest.route) {
            CameraPreviewScreen(
                onGoBackWithImage = navController::goBackWithImage,
                onGoBack = navController::popBackStack
            )
        }

        // trail info screen
        composable(
            route = TrailInfoDest.route,
            arguments = TrailInfoDest.arguments
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(TrailInfoDest.idArg)
            TrailInfoScreen(
                trailId = id,
                onGoBack = navController::popBackStack
            )
        }
    }
}