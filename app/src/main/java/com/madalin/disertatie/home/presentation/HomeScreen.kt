package com.madalin.disertatie.home.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.madalin.disertatie.core.presentation.navigation.DiscoverDest
import com.madalin.disertatie.core.presentation.navigation.HomeDestination
import com.madalin.disertatie.core.presentation.navigation.MapDest
import com.madalin.disertatie.core.presentation.navigation.ProfileDest
import com.madalin.disertatie.core.presentation.navigation.navigateSingleTopToIfDifferent
import com.madalin.disertatie.core.presentation.navigation.navigateToMapWithTrailId
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.presentation.navigation.HomeNavHost

@Composable
fun HomeScreen(
    trailIdToShowOnMap: String? = null,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    onNavigateToTrailInfoWithTrailId: (String) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { HomeBottomBar(navController = navController) }
    ) { paddingValues ->
        HomeNavHost(
            navController = navController,
            paddingValues = paddingValues,
            onNavigateToCameraPreview = { onNavigateToCameraPreview() },
            onGetImageResultOnce = { onGetImageResultOnce() },
            onNavigateToTrailInfo = onNavigateToTrailInfoWithTrailId
        )

        // TODO invalidate trail ID argument one screen resume
        // if this screen receives a trail ID, it will launch the map screen
        LaunchedEffect(key1 = trailIdToShowOnMap) {
            if (trailIdToShowOnMap != null) {
                navController.navigateToMapWithTrailId(trailIdToShowOnMap)
            }
        }
    }
}

@Composable
private fun HomeTopBar(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.statusBarsPadding()) {
        Button(onClick = { /*TODO*/ }) {
            Text(text = "dummy button")
        }
    }
}

@Composable
private fun HomeBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(DiscoverDest, MapDest, ProfileDest)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = Dimens.container)
            .clip(MaterialTheme.shapes.large),
        windowInsets = WindowInsets(bottom = 0)
    ) {
        destinations.forEach { destination ->
            AddItem(
                destination = destination,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
private fun RowScope.AddItem(
    destination: HomeDestination,
    currentDestination: NavDestination?,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
        onClick = { navController.navigateSingleTopToIfDifferent(destination.route) },
        icon = { Icon(imageVector = destination.icon, contentDescription = destination.title.asString()) },
        modifier = modifier,
        label = { Text(text = destination.title.asString()) }
    )
}