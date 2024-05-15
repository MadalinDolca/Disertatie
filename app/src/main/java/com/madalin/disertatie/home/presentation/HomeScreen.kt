package com.madalin.disertatie.home.presentation

import android.app.Activity
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.extensions.hasSameCoordinates
import com.madalin.disertatie.home.domain.extensions.toLatLng
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
                viewModel.showStatusBanner(StatusBannerType.Success, R.string.device_location_has_been_enabled)
                viewModel.setLocationAvailability(true)
                viewModel.startLocationFetching(applicationContext)
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
            HomeBottomBar(onLogoutClick = viewModel::logout)
        },
        floatingActionButton = {
            CreateTrailFAB(
                isCreatingTrail = uiState.isCreatingTrail,
                onStartTrailCreationClick = viewModel::startTrailCreation,
                onStopTrailCreationClick = viewModel::stopTrailCreation
            )
        }
    ) { paddingValues ->
        Box {
            MapContainer(
                cameraPositionState = uiState.cameraPositionState,
                mapUiSettings = uiState.mapUiSettings,
                mapProperties = uiState.mapProperties,
                isLocationAvailable = uiState.isLocationAvailable,
                isCreatingTrail = uiState.isCreatingTrail,
                userLocation = uiState.currentUserLocation,
                trailPointsList = uiState.trailPointsList,
                onEnableLocationClick = { viewModel.enableLocationSettings(applicationContext, locationSettingResultRequest) },
                onSetCameraDraggedState = viewModel::setCameraDraggedState
            )

            MapControls(
                paddingValues = paddingValues,
                userLocation = uiState.currentUserLocation,
                cameraPosition = uiState.cameraPositionState.position,
                isCreatingTrail = uiState.isCreatingTrail,
                onMoveCameraClick = viewModel::moveCameraToUserLocation,
                onAddTrailInfoClick = { }
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
    onSetCameraDraggedState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // camera position is changing due to a user gesture (dragging)
        LaunchedEffect(cameraPositionState.isMoving) {
            if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                onSetCameraDraggedState(true)
            }
        }

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
private fun MapControls(
    paddingValues: PaddingValues,
    userLocation: Location?,
    cameraPosition: CameraPosition,
    isCreatingTrail: Boolean,
    onMoveCameraClick: () -> Unit,
    onAddTrailInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding() + Dimens.container)
    ) {
        // right controls
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(Dimens.separator)
        ) {
            AddTrailInfoButton(
                isCreatingTrail = isCreatingTrail,
                onClick = onAddTrailInfoClick
            )
        }

        // left controls
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            AnimatedVisibility(
                visible = !isCameraPositionedOnUser(cameraPosition, userLocation),
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                UserLocationButton(
                    userLocation = userLocation,
                    cameraPosition = cameraPosition,
                    onMoveCameraClick = onMoveCameraClick,
                    modifier = Modifier.padding(start = Dimens.container)
                )
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
private fun CreateTrailFAB(
    isCreatingTrail: Boolean,
    onStartTrailCreationClick: () -> Unit,
    onStopTrailCreationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        text = {
            Text(
                text = if (!isCreatingTrail) stringResource(R.string.create_trail)
                else stringResource(R.string.stop)
            )
        },
        icon = { },
        onClick = {
            if (!isCreatingTrail) onStartTrailCreationClick()
            else onStopTrailCreationClick()
        },
        modifier = modifier
    )
}

@Composable
private fun UserLocationButton(
    userLocation: Location?,
    cameraPosition: CameraPosition,
    onMoveCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onMoveCameraClick,
        modifier = modifier.size(Dimens.iconButtonContainerSize),
        enabled = userLocation != null,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.2f),
            disabledContentColor = Color.Black.copy(alpha = 0.2f)
        )
    ) {
        Icon(
            imageVector = if (isCameraPositionedOnUser(cameraPosition, userLocation)) {
                Icons.Rounded.MyLocation
            } else {
                Icons.Rounded.LocationSearching
            },
            contentDescription = "User location",
            modifier = Modifier.size(Dimens.iconButtonContentSize)
        )
    }
}

@Composable
private fun AddTrailInfoButton(
    isCreatingTrail: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Dimens.iconButtonContainerSize),
        colors = IconButtonDefaults.filledIconButtonColors()
    ) {
        Icon(
            imageVector = Icons.Rounded.PostAdd,
            contentDescription = "Add trail info",
            modifier = Modifier.size(Dimens.iconButtonContentSize)
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
            .navigationBarsPadding()
            .padding(horizontal = Dimens.container)
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

/**
 * Checks if the [camera position][cameraPosition] is positioned on the
 * [user's location][userLocation] by checking if their coordinates are the same.
 * @return `true` if positioned, `false` otherwise.
 */
@Composable
private fun isCameraPositionedOnUser(cameraPosition: CameraPosition, userLocation: Location?): Boolean {
    return if (userLocation != null) {
        cameraPosition.target hasSameCoordinates userLocation.toLatLng()
    } else {
        false
    }
}