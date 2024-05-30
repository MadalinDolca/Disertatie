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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WindPower
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
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.presentation.SuggestionDialogState
import com.madalin.disertatie.map.presentation.action.SelectedTrailPointAction
import com.madalin.disertatie.map.presentation.action.SuggestionAction
import com.madalin.disertatie.map.presentation.util.CameraPermissionHandler
import kotlinx.coroutines.launch
import java.util.Date

/**
 * [ModalBottomSheet] used to display, add and modify [trailPoint] info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailPointInfoModal(
    isVisible: Boolean,
    sheetState: SheetState,
    trailPoint: TrailPoint,
    suggestionDialogState: SuggestionDialogState,
    isLoadingWeather: Boolean,
    isActivitySuggestionsDialogVisible: Boolean,
    isLoadingSuggestion: Boolean,
    onDismiss: () -> Unit,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    onAction: (Action) -> Unit,
    onUpdateTrailPointClick: (onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var isImageViewerDialogVisible by rememberSaveable { mutableStateOf(false) }
    var selectedImage: TrailImage? by rememberSaveable { mutableStateOf(null) }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
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
                    onAction(SelectedTrailPointAction.AddImage(LocalContext.current.applicationContext, it))
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
                    onValueChange = { onAction(SelectedTrailPointAction.UpdateNote(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(R.string.add_notes)) },
                    trailingIcon = {
                        IconButton(onClick = { onAction(SuggestionAction.ShowSuggestionDialog) }) {
                            Icon(imageVector = Icons.Rounded.SmartToy, contentDescription = "Ask AI")
                        }
                    },
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )

                WeatherCard(
                    isLoadingWeather = isLoadingWeather,
                    weather = trailPoint.weather,
                    onGetWeather = { onAction(SelectedTrailPointAction.GetWeather) },
                    onDeleteWeather = { onAction(SelectedTrailPointAction.DeleteWeather) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.mark_as_warning))
                    Switch(
                        checked = trailPoint.hasWarning,
                        onCheckedChange = { onAction(SelectedTrailPointAction.UpdateWarningState(it)) }
                    )
                }

                GeographicalData(trailPoint = trailPoint)

                ButtonsRow(
                    sheetState = sheetState,
                    onDismiss = { onDismiss() },
                    onSTPAction = onAction,
                    onUpdateTrailPointClick = onUpdateTrailPointClick
                )
            }
        }
    }

    if (selectedImage != null) {
        ImageViewerDialog(
            isVisible = isImageViewerDialogVisible,
            trailImage = selectedImage!!,
            onDelete = {
                onAction(SelectedTrailPointAction.RemoveImage(selectedImage!!))
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
                Card(
                    onClick = { onImageClick(trailImage) },
                    modifier = Modifier.size(80.dp)
                ) {
                    Image(
                        bitmap = trailImage.image.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
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
            WeatherInfo(weather = weather)
        }
    }
}

@Composable
private fun WeatherInfo(
    weather: Weather,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(Dimens.container)) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                SubcomposeAsyncImage(
                    model = "https://openweathermap.org/img/wn/${weather.weatherIcon}@2x.png",
                    loading = { CircularProgressIndicator() },
                    contentDescription = "Weather icon",
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${weather.weatherMain}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${weather.weatherDescription}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(Dimens.separator))

            Column {
                Text(text = "${weather.locationName}")
                Text(
                    text = "${weather.mainTemperature} " + stringResource(R.string.unit_celsius_degrees),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.feels_like_x_celsius_degrees, "${weather.mainFeelsLike}"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Row {
                    Icon(imageVector = Icons.Rounded.WindPower, contentDescription = "Wind speed")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.windSpeed} " + stringResource(R.string.unit_meters_per_second))
                }
                Row {
                    Icon(imageVector = Icons.Rounded.Cloud, contentDescription = "Clouds")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.clouds} " + stringResource(R.string.unit_percent))
                }
            }

            Spacer(modifier = Modifier.width(Dimens.separator * 2))

            Column {
                Row {
                    Icon(imageVector = Icons.Rounded.Compress, contentDescription = "Pressure")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.mainPressure} " + stringResource(R.string.unit_atmospheric_pressure_on_the_sea_level))
                }
                Row {
                    Icon(imageVector = Icons.Rounded.WaterDrop, contentDescription = "Humidity")
                    Spacer(modifier = Modifier.width(Dimens.separator))
                    Text(text = "${weather.mainHumidity} " + stringResource(R.string.unit_percent))
                }
            }
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
            val date = Date(trailPoint.timestamp)

            Text(text = stringResource(R.string.latitude) + ": " + trailPoint.latitude + latitudeNotation)
            Text(text = stringResource(R.string.longitude) + ": " + trailPoint.longitude + longitudeNotation)
            Text(text = stringResource(R.string.altitude) + ": " + trailPoint.altitude)
            Text(text = stringResource(R.string.timestamp) + ": " + date)
            Text(text = stringResource(R.string.accuracy) + ": " + trailPoint.accuracy)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonsRow(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSTPAction: (SelectedTrailPointAction) -> Unit,
    onUpdateTrailPointClick: (onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // clear button
        FilledTonalButton(
            onClick = { onSTPAction(SelectedTrailPointAction.ClearData) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(text = stringResource(R.string.clear))
        }

        // cancel button
        FilledTonalButton(onClick = {
            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) onDismiss()
            }
        }) {
            Text(text = stringResource(R.string.cancel))
        }

        // save button
        Button(onClick = {
            onUpdateTrailPointClick(
                { // success
                    coroutineScope.launch { sheetState.hide() }
                        .invokeOnCompletion { if (!sheetState.isVisible) onDismiss() }
                },
                {} // failure
            )
        }) {
            Text(text = stringResource(R.string.save))
        }
    }
}