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
import com.madalin.disertatie.core.presentation.navigation.DiscoverDest
import com.madalin.disertatie.core.presentation.navigation.MapDest
import com.madalin.disertatie.core.presentation.navigation.ProfileDest
import com.madalin.disertatie.discover.DiscoverScreen
import com.madalin.disertatie.map.presentation.MapScreen
import com.madalin.disertatie.profile.presentation.ProfileScreen

@Composable
fun HomeNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    onNavigateToTrailInfo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MapDest.route,
        modifier = modifier
    ) {
        // discover screen
        composable(
            route = DiscoverDest.route,
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
            DiscoverScreen(
                paddingValues = paddingValues,
                onNavigateToTrailInfo = { onNavigateToTrailInfo(it) }
            )
        }

        // map screen
        composable(route = MapDest.route) {
            MapScreen(
                paddingValues = paddingValues,
                onNavigateToCameraPreview = { onNavigateToCameraPreview() },
                onGetImageResultOnce = { onGetImageResultOnce() },
                onNavigateToTrailInfo = { onNavigateToTrailInfo(it) }
            )
        }

        // profile screen
        composable(route = ProfileDest.route,
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
            }
        ) {
            ProfileScreen(
                paddingValues = paddingValues,
                onNavigateToTrailInfo = { onNavigateToTrailInfo(it) }
            )
        }
    }
}