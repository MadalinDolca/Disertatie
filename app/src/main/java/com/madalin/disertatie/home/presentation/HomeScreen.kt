package com.madalin.disertatie.home.presentation

import android.app.Activity
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
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
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.components.StatusBannerType
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.extensions.toLatLng
import com.madalin.disertatie.home.domain.model.TrailPoint
import com.madalin.disertatie.home.presentation.components.LocationNotAvailableBanner
import com.madalin.disertatie.home.presentation.components.TrailPointInfoMarker
import com.madalin.disertatie.home.presentation.components.TrailPointInfoModal
import com.madalin.disertatie.home.presentation.components.UserMarker
import com.madalin.disertatie.home.presentation.util.LocationPermissionsHandler
import com.madalin.disertatie.home.presentation.util.bitmapDescriptor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                isLocationAvailable = uiState.isLocationAvailable,
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
                onShowTrailPointInfoModalClick = viewModel::showTrailPointInfoModal
            )

            MapControls(
                paddingValues = paddingValues,
                userLocation = uiState.currentUserLocation,
                isCreatingTrail = uiState.isCreatingTrail,
                isUserLocationButtonVisible = viewModel.isUserLocationButtonVisible(),
                onMoveCameraClick = viewModel::moveCameraToUserLocation,
                onShowTrailPointInfoModalClick = viewModel::showTrailPointInfoModal
            )

            uiState.selectedTrailPoint?.let { selectedTrailPoint ->
                TrailPointInfoModal(
                    isVisible = uiState.isTrailPointInfoModalVisible,
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true, // fully expanded
                        confirmValueChange = { false } // not dismissible when clicked outside of the sheet
                    ),
                    trailPoint = selectedTrailPoint,
                    onDismiss = viewModel::hideTrailPointInfoModal,
                    onTakePictureClick = {},
                    onAddWeatherInfoClick = {},
                    onUpdateTrailPointClick = { imagesList, note, hasWarning ->
                        viewModel.updateTrailPoint(
                            imagesList = imagesList,
                            note = note,
                            hasWarning = hasWarning
                        )
                    }
                )
            }
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
    onShowTrailPointInfoModalClick: (TrailPoint?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (userLocation != null) {
                UserMarker(coordinates = userLocation.toLatLng())
            }

            // path will not be shown if creation is stopped
            if (isCreatingTrail) {
                /* trailPointsList.firstOrNull()?.let {
                     TrailStartMarker(
                         coordinates = it.toLatLng(),
                         trail = null
                     )
                 }*/

                // shows the info markers on the map
                for (trailPoint in trailPointsList) {
                    if (trailPoint.note.isNotEmpty()
                        || trailPoint.imagesList.isNotEmpty()
                        || trailPoint.hasWarning
                    ) {
                        TrailPointInfoMarker(
                            trailPoint = trailPoint,
                            onClick = { onShowTrailPointInfoModalClick(trailPoint) }
                        )
                    }
                }

                val startCapBitmap = bitmapDescriptor(context = LocalContext.current, vectorResId = R.drawable.dot)!!
                Polyline(
                    points = trailPointsList.map { LatLng(it.latitude, it.longitude) },
                    clickable = true,
                    color = Color(0xFF0040FF),
                    startCap = CustomCap(startCapBitmap),
                    endCap = RoundCap(),
                    width = 15f,
                    onClick = { Log.d("MapContainer", "polyline clicked") }
                )
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
    isCreatingTrail: Boolean,
    isUserLocationButtonVisible: Boolean,
    onMoveCameraClick: () -> Unit,
    onShowTrailPointInfoModalClick: () -> Unit,
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
            AnimatedVisibility(
                visible = isCreatingTrail,
                enter = expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                AddTrailInfoButton(
                    onClick = onShowTrailPointInfoModalClick,
                    modifier = Modifier.padding(end = Dimens.container)
                )
            }
        }

        // left controls
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            AnimatedVisibility(
                visible = isUserLocationButtonVisible,
                enter = expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                UserLocationButton(
                    userLocation = userLocation,
                    onClick = onMoveCameraClick,
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
    isLocationAvailable: Boolean,
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
        icon = { Icon(imageVector = Icons.Rounded.Route, contentDescription = null) },
        onClick = {
            if (!isCreatingTrail) onStartTrailCreationClick()
            else onStopTrailCreationClick()
        },
        modifier = modifier,
        expanded = isLocationAvailable || isCreatingTrail,
        containerColor = if (isLocationAvailable) {
            FloatingActionButtonDefaults.containerColor
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    )
}

@Composable
private fun UserLocationButton(
    userLocation: Location?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
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
            imageVector = if (userLocation != null) {
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