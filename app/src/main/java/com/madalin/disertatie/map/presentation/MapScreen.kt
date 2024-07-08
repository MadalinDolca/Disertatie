package com.madalin.disertatie.map.presentation

import android.app.Activity
import android.graphics.Bitmap
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OnlinePrediction
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.extension.prettyLength
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.util.NEARBY_TRAIL_MIN_DISTANCE
import com.madalin.disertatie.core.presentation.components.LoadingDialog
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.domain.extension.toLatLng
import com.madalin.disertatie.map.presentation.action.LocationAction
import com.madalin.disertatie.map.presentation.action.MapAction
import com.madalin.disertatie.map.presentation.action.TrailAction
import com.madalin.disertatie.map.presentation.components.ExtremePointType
import com.madalin.disertatie.map.presentation.components.TrailEndDialog
import com.madalin.disertatie.map.presentation.components.TrailExtremePointMarker
import com.madalin.disertatie.map.presentation.components.TrailPointInfoMarker
import com.madalin.disertatie.map.presentation.components.TrailPointInfoModal
import com.madalin.disertatie.map.presentation.components.UnavailableLocationBanner
import com.madalin.disertatie.map.presentation.components.UserMarker
import com.madalin.disertatie.map.presentation.components.icons.rememberLineEndCircle
import com.madalin.disertatie.map.presentation.components.icons.rememberLineStartCircle
import com.madalin.disertatie.map.presentation.util.LocationPermissionsHandler
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    paddingValues: PaddingValues,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    onNavigateToTrailInfo: (trailId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationContext = LocalContext.current.applicationContext

    val locationSettingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                viewModel.handleAction(LocationAction.SettingResultEnabled(applicationContext))
            } else {
                viewModel.handleAction(LocationAction.SettingResultDisabled)
            }
        }
    )
    val enableLocationSettingsLambda = remember { { viewModel.enableLocationSettings(applicationContext, locationSettingResultRequest) } }

    LocationPermissionsHandler(onPermissionGranted = enableLocationSettingsLambda)

    MapContainer(
        cameraPositionState = uiState.cameraPositionState,
        mapUiSettings = uiState.mapUiSettings,
        mapProperties = uiState.mapProperties,
        isCreatingTrail = uiState.isCreatingTrail,
        isLaunchedTrail = uiState.isLaunchedTrail,
        userLocation = uiState.currentUserLocation,
        currentTrail = uiState.currentTrail,
        areNearbyTrailsVisible = uiState.areNearbyTrailsVisible,
        nearbyTrails = uiState.nearbyTrails,
        onAction = viewModel::handleAction,
        onNavigateToTrailInfo = { onNavigateToTrailInfo(it) }
    )

    MapControls(
        paddingValues = paddingValues,
        userLocation = uiState.currentUserLocation,
        isLocationAvailable = uiState.isLocationAvailable,
        isCreatingTrail = uiState.isCreatingTrail,
        isUserLocationButtonVisible = viewModel.isUserLocationButtonVisible(),
        isLaunchedTrail = uiState.isLaunchedTrail,
        areNearbyTrailsVisible = uiState.areNearbyTrailsVisible,
        nearbyTrailsCount = uiState.nearbyTrails.size,
        onAction = viewModel::handleAction
    )

    uiState.selectedTrailPoint?.let { selectedTrailPoint ->
        TrailPointInfoModal(
            isVisible = uiState.isTrailPointInfoModalVisible,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true, // fully expanded
                //confirmValueChange = { false } // not dismissible when clicked outside of the sheet
            ),
            isOwner = uiState.currentTrail?.userId == uiState.currentUser?.id,
            trailPoint = selectedTrailPoint,
            suggestionDialogState = uiState.suggestionDialogState,
            isLoadingWeather = uiState.isLoadingWeather,
            isActivitySuggestionsDialogVisible = uiState.isActivitySuggestionsDialogVisible,
            isLoadingSuggestion = uiState.isLoadingSuggestion,
            onAction = viewModel::handleAction,
            onNavigateToCameraPreview = { onNavigateToCameraPreview() },
            onGetImageResultOnce = { onGetImageResultOnce() }
        )
    }

    UnavailableLocationBanner(
        modifier = Modifier
            .padding(Dimens.container)
            .statusBarsPadding(),
        isVisible = !uiState.isLocationAvailable,
        onEnableLocationClick = enableLocationSettingsLambda
    )

    uiState.currentTrail?.let {
        TrailEndDialog(
            isVisible = uiState.isTrailEndDialogVisible,
            isUploading = uiState.isTrailUploading,
            trail = it,
            trailNameError = uiState.trailNameError,
            onAction = viewModel::handleAction
        )
    }

    LoadingDialog(
        isVisible = uiState.isLoadingLaunchedTrail,
        message = stringResource(R.string.loading_trail),
        button = {
            Spacer(modifier = Modifier.height(Dimens.separator))
            TextButton(onClick = { viewModel.handleAction(TrailAction.CancelLaunchedTrailLoading) }) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun MapContainer(
    cameraPositionState: CameraPositionState,
    mapUiSettings: MapUiSettings,
    mapProperties: MapProperties,
    isCreatingTrail: Boolean,
    isLaunchedTrail: Boolean,
    userLocation: Location?,
    currentTrail: Trail?,
    areNearbyTrailsVisible: Boolean,
    nearbyTrails: List<Trail>,
    onAction: (Action) -> Unit,
    onNavigateToTrailInfo: (trailId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        if (userLocation != null) {
            UserMarker(coordinates = userLocation.toLatLng())
        }

        // path will not be shown if creation is stopped and the trail doesn't exist
        // or if no trail has been launched and the current trail doesn't exist
        if (isCreatingTrail && currentTrail != null
            || isLaunchedTrail && currentTrail != null
        ) {
            // shows the info markers on the map
            for (trailPoint in currentTrail.trailPointsList) {
                if (trailPoint.note.isNotEmpty()
                    || trailPoint.imagesList.isNotEmpty()
                    || trailPoint.weather != null
                    || trailPoint.hasWarning
                ) {
                    TrailPointInfoMarker(
                        trailPoint = trailPoint,
                        onClick = { onAction(TrailAction.ShowTrailPointInfoModal(trailPoint)) }
                    )
                }
            }

            Polyline(
                points = currentTrail.trailPointsList.map { LatLng(it.latitude, it.longitude) },
                clickable = false,
                color = Color(0xFF0040FF),
                startCap = RoundCap(),
                endCap = RoundCap(),
                width = 15f
            )
        }

        // shows the extreme points markers if a trail has been launched
        if (isLaunchedTrail && currentTrail != null) {
            TrailExtremePointMarker(
                type = ExtremePointType.START,
                coordinates = currentTrail.trailPointsList.first().toLatLng(),
                trail = currentTrail
            )
            TrailExtremePointMarker(
                type = ExtremePointType.END,
                coordinates = currentTrail.trailPointsList.last().toLatLng(),
                trail = currentTrail
            )
        }

        // shows the nearby trails and a radius if they are set to visible
        if (areNearbyTrailsVisible) {
            userLocation?.let {
                Circle(
                    center = it.toLatLng(),
                    radius = NEARBY_TRAIL_MIN_DISTANCE.toDouble(),
                    strokeColor = Color.Red,
                    strokeWidth = 10f
                )
            }

            nearbyTrails.forEach { trail ->
                Polyline(
                    points = trail.trailPointsList.map { LatLng(it.latitude, it.longitude) },
                    clickable = true,
                    color = remember {
                        Color(
                            red = Random.nextInt(256),
                            green = Random.nextInt(256),
                            blue = Random.nextInt(256)
                        )
                    },
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    width = 15f,
                    onClick = { onNavigateToTrailInfo(trail.id) }
                )
            }
        }
    }
}

@Composable
private fun MapControls(
    paddingValues: PaddingValues,
    userLocation: Location?,
    isLocationAvailable: Boolean,
    isCreatingTrail: Boolean,
    isUserLocationButtonVisible: Boolean,
    isLaunchedTrail: Boolean,
    areNearbyTrailsVisible: Boolean,
    nearbyTrailsCount: Int,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding() + Dimens.container)
    ) {
        CreateTrailFAB(
            isVisible = !isLaunchedTrail,
            isLocationAvailable = isLocationAvailable,
            isCreatingTrail = isCreatingTrail,
            onStartTrailCreationClick = { onAction(TrailAction.StartTrailCreation) },
            onStopTrailCreationClick = { onAction(TrailAction.StopTrailCreation) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimens.container)
        )

        LeftSideControls(
            isLaunchedTrail = isLaunchedTrail,
            isUserLocationButtonVisible = isUserLocationButtonVisible,
            userLocation = userLocation,
            onLaunchedTrailStartingPointClick = { onAction(MapAction.MoveCameraToLaunchedTrailStartingPoint) },
            onLaunchedTrailEndingPointClick = { onAction(MapAction.MoveCameraToLaunchedTrailEndingPoint) },
            onUserLocationButtonClick = { onAction(MapAction.MoveCameraToUserLocation) },
            modifier = Modifier.fillMaxSize()
        )

        RightSideControls(
            isCreatingTrail = isCreatingTrail,
            isLaunchedTrail = isLaunchedTrail,
            areNearbyTrailsVisible = areNearbyTrailsVisible,
            onShowTrailPointInfoModalClick = { onAction(TrailAction.ShowTrailPointInfoModal()) },
            onCloseLaunchedTrailClick = { onAction(TrailAction.CloseLaunchedTrail) },
            onShowNearbyTrailsClick = { onAction(TrailAction.ShowNearbyTrails) },
            onHideNearbyTrailsClick = { onAction(TrailAction.HideNearbyTrails) },
            modifier = Modifier.fillMaxSize()
        )

        NearbyTrailsInfoBanner(
            isVisible = areNearbyTrailsVisible,
            nearbyTrailsCount = nearbyTrailsCount,
            radius = NEARBY_TRAIL_MIN_DISTANCE,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = Dimens.container)
        )
    }
}

@Composable
private fun LeftSideControls(
    isLaunchedTrail: Boolean,
    isUserLocationButtonVisible: Boolean,
    userLocation: Location?,
    onLaunchedTrailStartingPointClick: () -> Unit,
    onLaunchedTrailEndingPointClick: () -> Unit,
    onUserLocationButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.separator, Alignment.Bottom),
    ) {
        // launched trail starting point
        LaunchedTrailExtremePointButton(
            isVisible = isLaunchedTrail,
            icon = rememberLineStartCircle(),
            onClick = { onLaunchedTrailStartingPointClick() },
            modifier = Modifier.padding(start = Dimens.container)
        )

        // launched trail ending point
        LaunchedTrailExtremePointButton(
            isVisible = isLaunchedTrail,
            icon = rememberLineEndCircle(),
            onClick = { onLaunchedTrailEndingPointClick() },
            modifier = Modifier.padding(start = Dimens.container)
        )

        // user location button
        UserLocationButton(
            isVisible = isUserLocationButtonVisible,
            userLocation = userLocation,
            onClick = { onUserLocationButtonClick() },
            modifier = Modifier.padding(start = Dimens.container)
        )
    }
}

@Composable
private fun RightSideControls(
    isCreatingTrail: Boolean,
    isLaunchedTrail: Boolean,
    areNearbyTrailsVisible: Boolean,
    onShowTrailPointInfoModalClick: () -> Unit,
    onCloseLaunchedTrailClick: () -> Unit,
    onShowNearbyTrailsClick: () -> Unit,
    onHideNearbyTrailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.separator, Alignment.CenterVertically),
        horizontalAlignment = Alignment.End
    ) {
        CloseLaunchedTrailButton(
            isVisible = isLaunchedTrail,
            onClick = { onCloseLaunchedTrailClick() },
            modifier = Modifier.padding(end = Dimens.container)
        )

        AddTrailInfoButton(
            isVisible = isCreatingTrail,
            onClick = { onShowTrailPointInfoModalClick() },
            modifier = Modifier.padding(end = Dimens.container)
        )

        ToggleNearbyTrailsButton(
            isTurnedOn = areNearbyTrailsVisible,
            onShowNearbyTrailsClick = { onShowNearbyTrailsClick() },
            onHideNearbyTrailsClick = { onHideNearbyTrailsClick() },
            modifier = Modifier.padding(end = Dimens.container)
        )
    }
}

@Composable
private fun NearbyTrailsInfoBanner(
    isVisible: Boolean,
    nearbyTrailsCount: Int,
    radius: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
        )
    ) {
        Card {
            Text(
                text = stringResource(
                    R.string.found_x_trails_within_a_radius_of_y,
                    nearbyTrailsCount,
                    radius.prettyLength()
                ),
                modifier = Modifier.padding(Dimens.container)
            )
        }
    }
}

@Composable
private fun CreateTrailFAB(
    isVisible: Boolean,
    isLocationAvailable: Boolean,
    isCreatingTrail: Boolean,
    onStartTrailCreationClick: () -> Unit,
    onStopTrailCreationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        val currentView = LocalView.current
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = if (!isCreatingTrail) stringResource(R.string.create_trail)
                    else stringResource(R.string.stop)
                )
            },
            icon = { Icon(imageVector = Icons.Rounded.Route, contentDescription = null) },
            onClick = {
                if (!isCreatingTrail) {
                    onStartTrailCreationClick()
                    currentView.keepScreenOn = true
                } else {
                    onStopTrailCreationClick()
                    currentView.keepScreenOn = false
                }
            },
            expanded = isLocationAvailable || isCreatingTrail,
            containerColor = if (isLocationAvailable) {
                FloatingActionButtonDefaults.containerColor
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    }
}

@Composable
private fun UserLocationButton(
    isVisible: Boolean,
    userLocation: Location?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        IconButton(
            onClick = { onClick() },
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
}

@Composable
private fun LaunchedTrailExtremePointButton(
    isVisible: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier.size(Dimens.iconButtonContainerSize),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Launched trail point",
                modifier = Modifier.size(Dimens.iconButtonContentSize)
            )
        }
    }
}

@Composable
private fun AddTrailInfoButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        IconButton(
            onClick = { onClick() },
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
}

@Composable
private fun CloseLaunchedTrailButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        IconButton(
            onClick = { onClick() },
            modifier = modifier.size(Dimens.iconButtonContainerSize),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Rocket,
                contentDescription = "Close launched trail",
                modifier = Modifier.size(Dimens.iconButtonContentSize)
            )
        }
    }
}

@Composable
private fun ToggleNearbyTrailsButton(
    isTurnedOn: Boolean,
    onShowNearbyTrailsClick: () -> Unit,
    onHideNearbyTrailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { if (isTurnedOn) onHideNearbyTrailsClick() else onShowNearbyTrailsClick() },
        modifier = modifier.size(Dimens.iconButtonContainerSize),
        colors = if (isTurnedOn) {
            IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        } else {
            IconButtonDefaults.filledIconButtonColors()
        }
    ) {
        Icon(
            imageVector = Icons.Rounded.OnlinePrediction,
            contentDescription = "Nearby trails",
            modifier = Modifier.size(Dimens.iconButtonContentSize)
        )
    }
}