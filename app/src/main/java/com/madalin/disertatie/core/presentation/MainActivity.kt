package com.madalin.disertatie.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import coil.Coil
import com.google.android.gms.maps.MapsInitializer
import com.madalin.disertatie.core.domain.util.initUntrustImageLoader
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.navigation.DisertatieNavHost
import com.madalin.disertatie.core.presentation.navigation.Home
import com.madalin.disertatie.core.presentation.navigation.Login
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(this)
        Coil.setImageLoader(initUntrustImageLoader(this))

        enableEdgeToEdge()
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value.isSplashScreenVisible
        }

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

    if (!uiState.isSplashScreenVisible) {
        val navController = rememberNavController()

        DisertatieNavHost(
            navController = navController,
            // automatically navigates to Home if user login state is true
            startDestination = if (uiState.isUserLoggedIn) Home.route else Login.route
        )
    }

    StatusBanner(
        isVisible = uiState.isStatusBannerVisible,
        data = uiState.statusBannerData,
        onDismiss = { viewModel.setStatusBannerVisibility(false) }
    )
}