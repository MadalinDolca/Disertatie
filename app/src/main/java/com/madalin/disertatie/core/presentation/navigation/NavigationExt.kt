package com.madalin.disertatie.core.presentation.navigation

import android.graphics.Bitmap
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navigates to the given [destination] with SingleTop behaviour.
 *
 * Pops backstack to start destination, preserves destination state and observes SingleTop.
 */
fun NavHostController.navigateSingleTopTo(destination: String) {
    this.navigate(destination) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigates to the given [destination] with SingleTop behaviour if it is different from the current
 * destination.
 */
fun NavHostController.navigateSingleTopToIfDifferent(destination: String) {
    this.currentBackStackEntry?.destination?.route?.let { currentRoute ->
        if (currentRoute != destination) {
            this.navigateSingleTopTo(destination)
        }
    }
}

private val NavHostController.canGoBack: Boolean
    get() = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

/**
 * Gets the image result from the previous screen, returns it and sets it to null.
 */
fun NavHostController.getImageResultOnce(): Bitmap? {
    val image = this.currentBackStackEntry?.savedStateHandle?.get<Bitmap>("capturedImage")
    this.currentBackStackEntry?.savedStateHandle?.set("capturedImage", null)
    return image
}

/**
 * Goes back to the previous screen with the given [image].
 */
fun NavHostController.goBackWithImage(image: Bitmap) {
    this.previousBackStackEntry?.savedStateHandle?.set("capturedImage", image) // result to be returned to the previous screen
    this.popBackStack() // go back to the previous screen
}

/**
 * Navigates to the trail info screen that has the given [trailId].
 */
fun NavHostController.navigateToTrailInfoWithTrailId(trailId: String) {
    this.navigateSingleTopTo(TrailInfoDest.routeBuilder(trailId))
}