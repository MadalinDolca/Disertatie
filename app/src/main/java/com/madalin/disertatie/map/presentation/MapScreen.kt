package com.madalin.disertatie.map.presentation

import android.app.Activity
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.domain.extension.toLatLng
import com.madalin.disertatie.map.presentation.action.LocationAction
import com.madalin.disertatie.map.presentation.action.MapAction
import com.madalin.disertatie.map.presentation.action.TrailAction
import com.madalin.disertatie.map.presentation.components.LocationNotAvailableBanner
import com.madalin.disertatie.map.presentation.components.TrailEndDialog
import com.madalin.disertatie.map.presentation.components.TrailPointInfoMarker
import com.madalin.disertatie.map.presentation.components.TrailPointInfoModal
import com.madalin.disertatie.map.presentation.components.UserMarker
import com.madalin.disertatie.map.presentation.util.LocationPermissionsHandler
import com.madalin.disertatie.map.presentation.util.bitmapDescriptor
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    trailIdToShow: String? = null,
    viewModel: MapViewModel = koinViewModel { parametersOf(trailIdToShow) },
    paddingValues: PaddingValues,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
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
        isLocationAvailable = uiState.isLocationAvailable,
        isCreatingTrail = uiState.isCreatingTrail,
        userLocation = uiState.currentUserLocation,
        currentTrail = uiState.currentTrail,
        onAction = viewModel::handleAction,
        onEnableLocationClick = enableLocationSettingsLambda
    )

    MapControls(
        paddingValues = paddingValues,
        userLocation = uiState.currentUserLocation,
        isLocationAvailable = uiState.isLocationAvailable,
        isCreatingTrail = uiState.isCreatingTrail,
        isUserLocationButtonVisible = viewModel.isUserLocationButtonVisible(),
        onAction = viewModel::handleAction
    )

    uiState.selectedTrailPoint?.let { selectedTrailPoint ->
        TrailPointInfoModal(
            isVisible = uiState.isTrailPointInfoModalVisible,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true, // fully expanded
                //confirmValueChange = { false } // not dismissible when clicked outside of the sheet
            ),
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

    uiState.currentTrail?.let {
        TrailEndDialog(
            isVisible = uiState.isTrailEndDialogVisible,
            isUploading = uiState.isTrailUploading,
            trail = it,
            trailNameError = uiState.trailNameError,
            onAction = viewModel::handleAction
        )
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
    currentTrail: Trail?,
    onAction: (Action) -> Unit,
    onEnableLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        val startCapBitmap = bitmapDescriptor(context = LocalContext.current, vectorResId = R.drawable.dot)!!

        if (userLocation != null) {
            UserMarker(coordinates = userLocation.toLatLng())
        }

        // path will not be shown if creation is stopped and the trail doesn't exist
        if (isCreatingTrail && currentTrail != null) {
            /* trailPointsList.firstOrNull()?.let {
                 TrailStartMarker(
                     coordinates = it.toLatLng(),
                     trail = null
                 )
             }*/

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

@Composable
private fun MapControls(
    paddingValues: PaddingValues,
    userLocation: Location?,
    isLocationAvailable: Boolean,
    isCreatingTrail: Boolean,
    isUserLocationButtonVisible: Boolean,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding() + Dimens.container)
    ) {
        CreateTrailFAB(
            isLocationAvailable = isLocationAvailable,
            isCreatingTrail = isCreatingTrail,
            onStartTrailCreationClick = { onAction(TrailAction.StartTrailCreation) },
            onStopTrailCreationClick = { onAction(TrailAction.StopTrailCreation) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimens.container)
        )

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
                    onClick = { onAction(TrailAction.ShowTrailPointInfoModal()) },
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
                    onClick = { onAction(MapAction.MoveCameraToUserLocation) },
                    modifier = Modifier.padding(start = Dimens.container)
                )
            }
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

@Composable
private fun AddTrailInfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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