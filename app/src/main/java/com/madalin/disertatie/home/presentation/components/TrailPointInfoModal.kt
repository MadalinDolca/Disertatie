package com.madalin.disertatie.home.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.model.TrailPoint
import com.madalin.disertatie.home.presentation.util.CameraPermissionHandler
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
    onDismiss: () -> Unit,
    onNavigateToCameraPreview: () -> Unit,
    onGetImageResultOnce: () -> Bitmap?,
    onAddWeatherInfoClick: () -> Unit,
    onUpdateSelectedTrailPoint: (update: (TrailPoint) -> TrailPoint) -> Unit,
    onUpdateTrailPointClick: (onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var isImageViewerDialogVisible by rememberSaveable { mutableStateOf(false) }
    var selectedImage: Bitmap? by rememberSaveable { mutableStateOf(null) }

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
                onGetImageResultOnce()?.let { bitmap ->
                    onUpdateSelectedTrailPoint { it.copy(imagesList = (it.imagesList + bitmap).toMutableList()) }
                }

                ImagesRow(
                    images = trailPoint.imagesList,
                    onImageClick = { image ->
                        selectedImage = image
                        isImageViewerDialogVisible = true
                    },
                    onOpenCamera = { onNavigateToCameraPreview() }
                )

                OutlinedTextField(
                    value = trailPoint.note,
                    onValueChange = { newValue -> onUpdateSelectedTrailPoint { it.copy(note = newValue) } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(R.string.add_notes)) },
                    trailingIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(imageVector = Icons.Rounded.SmartToy, contentDescription = "Ask AI")
                        }
                    },
                    maxLines = 3,
                    shape = MaterialTheme.shapes.medium
                )

                WeatherInfoCard(
                    onAddWeatherInfoClick = { onAddWeatherInfoClick() }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.mark_as_warning))
                    Switch(
                        checked = trailPoint.hasWarning,
                        onCheckedChange = { newValue -> onUpdateSelectedTrailPoint { it.copy(hasWarning = newValue) } }
                    )
                }

                GeographicalData(trailPoint = trailPoint)

                ButtonsRow(
                    sheetState = sheetState,
                    onDismiss = { onDismiss() },
                    onUpdateSelectedTrailPoint = onUpdateSelectedTrailPoint,
                    onUpdateTrailPointClick = onUpdateTrailPointClick
                )
            }
        }
    }

    if (selectedImage != null) {
        ImageViewerDialog(
            isVisible = isImageViewerDialogVisible,
            image = selectedImage!!,
            onDelete = {
                onUpdateSelectedTrailPoint {
                    val newImagesList = it.imagesList
                    newImagesList.remove(selectedImage)
                    it.copy(imagesList = newImagesList)
                }
                isImageViewerDialogVisible = false
            },
            onDismiss = { isImageViewerDialogVisible = false }
        )
    }
}

@Composable
private fun ImagesRow(
    images: List<Bitmap>,
    onImageClick: (Bitmap) -> Unit,
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
        if (images.size < 3) {
            Card(
                onClick = { isPermissionDialogVisible = true },
                modifier = Modifier.size(75.dp)
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
        images.forEach { image ->
            key(image.allocationByteCount) {
                Card(
                    onClick = { onImageClick(image) },
                    modifier = Modifier.size(75.dp)
                ) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherInfoCard(
    onAddWeatherInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onAddWeatherInfoClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.container),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.tap_to_add_weather_info))
            Icon(imageVector = Icons.Rounded.WbSunny, contentDescription = null)
        }
    }
}

@Composable
private fun GeographicalData(
    trailPoint: TrailPoint?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.container)) {
            val date = trailPoint?.timestamp?.let { Date(it) }
            Text(text = stringResource(R.string.latitude) + ": " + trailPoint?.latitude)
            Text(text = stringResource(R.string.longitude) + ": " + trailPoint?.longitude)
            Text(text = stringResource(R.string.altitude) + ": " + trailPoint?.altitude)
            Text(text = stringResource(R.string.timestamp) + ": " + date)
            Text(text = stringResource(R.string.accuracy) + ": " + trailPoint?.accuracy)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonsRow(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onUpdateSelectedTrailPoint: (update: (TrailPoint) -> TrailPoint) -> Unit,
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
            onClick = {
                onUpdateSelectedTrailPoint {
                    it.copy(
                        imagesList = mutableListOf(),
                        note = "",
                        hasWarning = false
                    )
                }
            },
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

/*@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
private fun TrailPointInfoModalPreview() {
    DisertatieTheme {
        Surface {
            TrailPointInfoModal(
                isVisible = true,
                sheetState = rememberStandardBottomSheetState(),
                trailPoint = TrailPoint(
                    latitude = 10.12345,
                    longitude = 21.01234,
                    altitude = 7.98765,
                    accuracy = 0f,
                    timestamp = 10L,
                    temperature = 0.0
                ),
                onDismiss = { },
                onNavigateToCameraPreview = { },
                onGetImageResultOnce = { null },
                onAddWeatherInfoClick = { },
                onUpdateTrailPointClick = { _, _, _ -> }
            )
        }
    }
}*/
