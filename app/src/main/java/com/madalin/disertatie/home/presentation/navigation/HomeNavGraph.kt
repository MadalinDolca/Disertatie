package com.madalin.disertatie.home.presentation.navigation

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.madalin.disertatie.discover.DiscoverScreen
import com.madalin.disertatie.map.presentation.MapScreen
import com.madalin.disertatie.profile.ProfileScreen

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.Map.route,
        modifier = modifier
    ) {
        // discover screen
        composable(
            route = HomeDestination.Discover.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            }
        ) {
            DiscoverScreen()
        }

        // map screen
        composable(route = HomeDestination.Map.route) {
            MapScreen(
                paddingValues = paddingValues,
                onNavigateToCameraPreview = { onNavigateToCameraPreview() },
                onGetImageResultOnce = { onGetImageResultOnce() }
            )
        }

        // profile screen
        composable(route = HomeDestination.Profile.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            }) {
            ProfileScreen()
        }
    }
}