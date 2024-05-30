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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.madalin.disertatie.core.presentation.navigation.navigateSingleTopTo
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.presentation.navigation.HomeDestination
import com.madalin.disertatie.home.presentation.navigation.HomeNavGraph

@Composable
fun HomeScreen(
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { HomeBottomBar(navController = navController) }
    ) { paddingValues ->
        HomeNavGraph(
            navController = navController,
            paddingValues = paddingValues,
            onNavigateToCameraPreview = { onNavigateToCameraPreview() },
            onGetImageResultOnce = { onGetImageResultOnce() }
        )
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
    val screens = listOf(
        HomeDestination.Discover,
        HomeDestination.Map,
        HomeDestination.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = Dimens.container)
            .clip(MaterialTheme.shapes.large),
        windowInsets = WindowInsets(bottom = 0)
    ) {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
private fun RowScope.AddItem(
    screen: HomeDestination,
    currentDestination: NavDestination?,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
        onClick = { navController.navigateSingleTopTo(screen.route) },
        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title.asString()) },
        modifier = modifier,
        label = { Text(text = screen.title.asString()) }
    )
}