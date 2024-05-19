package com.madalin.disertatie.home.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.home.domain.model.TrailPoint
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailPointInfoModalBottomSheet(
    isVisible: Boolean,
    sheetState: SheetState,
    trailPoint: TrailPoint,
    onDismiss: () -> Unit,
    onTakePictureClick: () -> Unit,
    onAddWeatherInfoClick: () -> Unit,
    onUpdateTrailPointClick: (List<String>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var note by remember { mutableStateOf(trailPoint.note ?: "") }
    val imagesList by remember { mutableStateOf(trailPoint.imagesList) }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier.height(LocalConfiguration.current.screenHeightDp.dp),
            sheetState = sheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.container),
                verticalArrangement = Arrangement.spacedBy(space = Dimens.separator)
            ) {
                ImagesRow(
                    onTakePictureClick = onTakePictureClick,
                    items = trailPoint.imagesList
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
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
                    onAddWeatherInfoClick = onAddWeatherInfoClick
                )

                GeographicalData(
                    trailPoint = trailPoint
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                    }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Button(onClick = {
                        onUpdateTrailPointClick(imagesList, note)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                    }) {
                        Text(text = stringResource(R.string.add_info))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagesRow(
    onTakePictureClick: () -> Unit,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        for (item in items) {
            Card(
                onClick = onTakePictureClick,
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

        if (items.size < 3) {
            Card(
                onClick = onTakePictureClick,
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
fun GeographicalData(
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

/*
@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
private fun AddTrailInfoDialogPreview() {
    DisertatieTheme {
        Surface {
            TrailPointInfoModalBottomSheet(
                isVisible = true,
                sheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.Expanded
                ),
                trailPoint = TrailPoint(
                    latitude = 10.12345,
                    longitude = 21.01234,
                    altitude = 7.98765,
                    accuracy = 0f,
                    timestamp = 10L,
                    note = null,
                    imagesList = emptyList(),
                    temperature = 0.0
                ),
                userLocation = null,
                onDismiss = {},
                onTakePictureClick = {},
                onUpdateNote = {},
                onAddWeatherInfoClick = {},
                onAddInfoClick = {}
            )
        }
    }
}*/
