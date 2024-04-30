package com.madalin.disertatie.core.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.navigation.DisertatieNavHost
import com.madalin.disertatie.core.presentation.navigation.Home
import com.madalin.disertatie.core.presentation.navigation.Login
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            DisertatieTheme {
                Surface {
                    DisertatieApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun DisertatieApp(viewModel: MainActivityViewModel) {
    val uiState by viewModel.state.collectAsState() //.collectAsStateWithLifecycle()

    Box {
        val navController = rememberNavController()
        DisertatieNavHost(
            navController = navController,
            // automatically navigates to Home if user login state is true
            startDestination = if (uiState.isUserLoggedIn) Home.route else Login.route
        )

        StatusBanner(
            isVisible = uiState.isStatusBannerVisible,
            data = uiState.statusBannerData,
            onDismiss = { viewModel.toggleStatusBannerVisibility(false) }
        )
    }
}