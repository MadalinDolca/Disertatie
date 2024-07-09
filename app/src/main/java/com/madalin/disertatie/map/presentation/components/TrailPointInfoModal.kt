package com.madalin.disertatie.map.presentation.components

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.extension.asDateAndTime
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.core.presentation.components.WeatherInfo
import com.madalin.disertatie.core.presentation.components.WeatherInfoMode
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.presentation.action.SuggestionAction
import com.madalin.disertatie.map.presentation.action.TrailAction
import com.madalin.disertatie.map.presentation.util.CameraPermissionHandler
import kotlinx.coroutines.launch

/**
 * [ModalBottomSheet] used to display, add and modify [trailPoint] info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailPointInfoModal(
    isVisible: Boolean,
    sheetState: SheetState,
    isOwner: Boolean,
    trailPoint: TrailPoint,
    suggestionDialogState: SuggestionDialogState,
    isLoadingWeather: Boolean,
    isActivitySuggestionsDialogVisible: Boolean,
    isLoadingSuggestion: Boolean,
    onAction: (Action) -> Unit,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    modifier: Modifier = Modifier
) {
    var isImageViewerDialogVisible by rememberSaveable { mutableStateOf(false) }
    var selectedImage: TrailImage? by rememberSaveable { mutableStateOf(null) }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onAction(TrailAction.HideTrailPointInfoModal) },
            modifier = modifier,
            sheetState = sheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.container),
                verticalArrangement = Arrangement.spacedBy(space = Dimens.separator)
            ) {
                onGetImageResultOnce()?.let {
                    onAction(TrailAction.AddStpImage(LocalContext.current.applicationContext, it))
                }

                ImagesRow(
                    trailImages = trailPoint.imagesList,
                    onImageClick = { image ->
                        selectedImage = image
                        isImageViewerDialogVisible = true
                    },
                    onOpenCamera = { onNavigateToCameraPreview() }
                )

                OutlinedTextField(
                    value = trailPoint.note,
                    onValueChange = { onAction(TrailAction.UpdateStpNote(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(R.string.add_notes)) },
                    trailingIcon = {
                        IconButton(onClick = { onAction(SuggestionAction.ShowSuggestionDialog) }) {
                            Icon(imageVector = Icons.Rounded.SmartToy, contentDescription = "Ask AI")
                        }
                    },
                    maxLines = 7,
                    shape = MaterialTheme.shapes.medium
                )

                WeatherCard(
                    isLoadingWeather = isLoadingWeather,
                    weather = trailPoint.weather,
                    onGetWeather = { onAction(TrailAction.GetStpWeather) },
                    onDeleteWeather = { onAction(TrailAction.DeleteStpWeather) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.mark_as_warning))
                    Switch(
                        checked = trailPoint.hasWarning,
                        onCheckedChange = { onAction(TrailAction.UpdateStpWarningState(it)) }
                    )
                }

                GeographicalData(trailPoint = trailPoint)

                ButtonsRow(
                    isOwner = isOwner,
                    sheetState = sheetState,
                    onAction = onAction,
                )
            }
        }
    }

    if (selectedImage != null) {
        ImageViewerDialog(
            isVisible = isImageViewerDialogVisible,
            isOwner = isOwner,
            trailImage = selectedImage!!,
            onDelete = {
                onAction(TrailAction.RemoveStpImage(selectedImage!!))
                isImageViewerDialogVisible = false
            },
            onDismiss = { isImageViewerDialogVisible = false }
        )
    }

    ActivitySuggestionsDialog(
        isVisible = isActivitySuggestionsDialogVisible,
        suggestionDialogState = suggestionDialogState,
        isSuggestionLoading = isLoadingSuggestion,
        trailPoint = trailPoint,
        onDismiss = { onAction(SuggestionAction.HideSuggestionDialog) },
        onAction = onAction
    )
}

@Composable
private fun ImagesRow(
    trailImages: List<TrailImage>,
    onImageClick: (TrailImage) -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPermissionDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (isPermissionDialogVisible) {
        CameraPermissionHandler(
            onPermissionGranted = { onOpenCamera() },
            onDismiss = { isPermissionDialogVisible = false }
        )
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space = Dimens.separator)
    ) {
        // add image button
        if (trailImages.size < 3) {
            Card(
                onClick = { isPermissionDialogVisible = true },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.container)
                )
            }
        }

        // image cards
        trailImages.forEach { trailImage ->
            key(trailImage.hashCode()) {
                ImageCard(
                    trailImage = trailImage,
                    onClick = { onImageClick(trailImage) },
                )
            }
        }
    }
}

@Composable
private fun ImageCard(
    trailImage: TrailImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick() },
        modifier = modifier.size(80.dp)
    ) {
        if (trailImage.imageUrl.isNotEmpty()) {
            SubcomposeAsyncImage(
                model = trailImage.imageUrl,
                contentDescription = null,
                loading = { CircularProgressIndicator() },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                bitmap = trailImage.image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeatherCard(
    isLoadingWeather: Boolean,
    weather: Weather?,
    onGetWeather: () -> Unit,
    onDeleteWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onGetWeather() },
                onLongClick = { onDeleteWeather() }
            )
    ) {
        if (weather == null && !isLoadingWeather) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.container),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.tap_to_obtain_weather_info))
                Icon(imageVector = Icons.Rounded.WbSunny, contentDescription = null)
            }
        } else if (isLoadingWeather) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.container),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (weather != null) {
            WeatherInfo(
                weather = weather,
                mode = WeatherInfoMode.SINGLE,
                modifier = modifier.padding(Dimens.container)
            )
        }
    }
}

@Composable
private fun GeographicalData(
    trailPoint: TrailPoint,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.container)) {
            val latitudeNotation = stringResource(
                if (trailPoint.latitude >= 0) R.string.unit_latitude_north
                else R.string.unit_latitude_south
            )
            val longitudeNotation = stringResource(
                if (trailPoint.longitude >= 0) R.string.unit_longitude_east
                else R.string.unit_longitude_west
            )

            Text(text = stringResource(R.string.latitude) + ": " + trailPoint.latitude + latitudeNotation)
            Text(text = stringResource(R.string.longitude) + ": " + trailPoint.longitude + longitudeNotation)
            Text(text = stringResource(R.string.timestamp) + ": " + trailPoint.timestamp.asDateAndTime())
            Text(text = stringResource(R.string.horizontal_accuracy_radius) + ": " + trailPoint.accuracy + " m")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonsRow(
    isOwner: Boolean,
    sheetState: SheetState,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (isOwner) {
            // clear button
            FilledTonalButton(
                onClick = { onAction(TrailAction.ClearStpData) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = stringResource(R.string.clear))
            }
        }

        // cancel button
        FilledTonalButton(onClick = {
            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) onAction(TrailAction.HideTrailPointInfoModal)
            }
        }) {
            Text(text = stringResource(R.string.cancel))
        }

        if (isOwner) {
            // save button
            Button(onClick = {
                onAction(TrailAction.UpdateTrailPoint(
                    { // success
                        coroutineScope.launch { sheetState.hide() }
                            .invokeOnCompletion { if (!sheetState.isVisible) onAction(TrailAction.HideTrailPointInfoModal) }
                    },
                    {} // failure
                ))
            }) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}