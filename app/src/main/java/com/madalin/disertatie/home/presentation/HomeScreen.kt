package com.madalin.disertatie.home.presentation

import android.app.Activity
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.model.TrailPoint
import com.madalin.disertatie.home.presentation.components.LocationNotAvailableBanner
import com.madalin.disertatie.home.presentation.components.UserMarker
import com.madalin.disertatie.home.presentation.util.LocationPermissionsHandler
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    MapsInitializer.initialize(LocalContext.current)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationContext = LocalContext.current.applicationContext

    val locationSettingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                viewModel.setLocationAvailability(true)
                viewModel.showStatusBanner(StatusBannerType.Success, R.string.device_location_has_been_enabled)
            } else {
                viewModel.setLocationAvailability(false)
            }
        }
    )

    LocationPermissionsHandler(onPermissionGranted = {
        viewModel.enableLocationSettings(applicationContext, locationSettingResultRequest)
    })

    Scaffold(
        topBar = {
            //HomeTopBar()
        },
        bottomBar = {
            HomeBottomBar(onLogoutClick = { viewModel.logout() })
        },
        floatingActionButton = {
            HomeFloatingActionButton(
                isTracking = uiState.isCreatingTrail,
                onStartCreateTrailClick = { viewModel.startTrailCreation(applicationContext) },
                onStopCreateTrailClick = { viewModel.stopTrailCreation() })
        }
    ) { padding ->
        Column(
            // modifier = Modifier.padding(padding)
        ) {
            MapContainer(
                cameraPositionState = uiState.cameraPositionState,
                mapUiSettings = uiState.mapUiSettings,
                mapProperties = uiState.mapProperties,
                isLocationAvailable = uiState.isLocationAvailable,
                isCreatingTrail = uiState.isCreatingTrail,
                userLocation = uiState.currentUserLocation,
                trailPointsList = uiState.trailPointsList,
                onEnableLocationClick = { viewModel.enableLocationSettings(applicationContext, locationSettingResultRequest) }
            )
        }
    }
}

@Composable
private fun MapContainer(
    cameraPositionState: CameraPositionState,
    mapUiSettings: MapUiSettings,
    mapProperties: MapProperties,
    isLocationAvailable: Boolean,
    isCreatingTrail: Boolean,
    userLocation: Location?,
    trailPointsList: List<TrailPoint>,
    onEnableLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        GoogleMap(
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // path will not be shown if creation is stopped
            if (isCreatingTrail) {
                Polyline(
                    points = trailPointsList.map { LatLng(it.latitude, it.longitude) },
                    color = Color.Red,
                    width = 10f
                )
            }

            if (userLocation != null) {
                UserMarker(userLocation = userLocation)
            }
        }

        LocationNotAvailableBanner(
            isVisible = !isLocationAvailable,
            onEnableLocationClick = { onEnableLocationClick() }
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
private fun HomeFloatingActionButton(
    isTracking: Boolean,
    onStartCreateTrailClick: () -> Unit,
    onStopCreateTrailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = {
        if (!isTracking) onStartCreateTrailClick()
        else onStopCreateTrailClick()
    }) {
        Text(
            text = if (!isTracking) stringResource(R.string.start_tracking)
            else stringResource(R.string.stop_tracking)
        )
    }
}

@Composable
private fun HomeBottomBar(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit
) {
    BottomAppBar(
        modifier = modifier
            .padding(Dimens.container)
            .clip(MaterialTheme.shapes.large),
        windowInsets = WindowInsets(bottom = 0)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = { Icon(imageVector = Icons.Rounded.Map, contentDescription = "Discover") },
            label = { Text(text = "what") }
        )

        NavigationBarItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = { Icon(imageVector = Icons.Rounded.Place, contentDescription = "Search") },
            label = { Text(text = "what") }
        )

        NavigationBarItem(
            selected = true,
            onClick = onLogoutClick,
            icon = { Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = "Discover") },
            label = { Text(text = "Logout") }
        )
    }
}