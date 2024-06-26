package com.madalin.disertatie.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import coil.Coil
import com.google.android.gms.maps.MapsInitializer
import com.madalin.disertatie.core.domain.util.initUntrustImageLoader
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.navigation.HomeDest
import com.madalin.disertatie.core.presentation.navigation.LoginDest
import com.madalin.disertatie.core.presentation.navigation.MainNavHost
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens
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
    val setStatusBannerVisibilityLambda = remember { { viewModel.setStatusBannerVisibility(false) } }

    if (!uiState.isSplashScreenVisible) {
        val navController = rememberNavController()

        MainNavHost(
            navController = navController,
            // automatically navigates to Home if user login state is true
            startDestination = if (uiState.isUserLoggedIn) HomeDest.route else LoginDest.route
        )
    }

    StatusBanner(
        isVisible = uiState.isStatusBannerVisible,
        data = uiState.statusBannerData,
        onDismiss = setStatusBannerVisibilityLambda,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.container)
            .statusBarsPadding()
    )
}