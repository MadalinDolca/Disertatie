package com.madalin.disertatie.map.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.presentation.util.getLocationTypeString

@Composable
fun ImageViewerDialog(
    isVisible: Boolean,
    trailImage: TrailImage,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(modifier = modifier.padding(Dimens.container)) {
                // shows the classification info if the image is not obtained from the backend
                if (trailImage.imageUrl.isEmpty()) {
                    ClassificationInfo(trailImage = trailImage)
                }

                Card {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio((trailImage.image.width.toFloat() / trailImage.image.height.toFloat()))
                    ) {
                        ImageViewer(
                            trailImage = trailImage,
                            constraints = this@BoxWithConstraints.constraints,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        ButtonsRow(
                            onDelete = { onDelete() },
                            onDismiss = { onDismiss() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = Dimens.separator)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassificationInfo(
    trailImage: TrailImage,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.separator)
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(Dimens.container)) {
            val topResultType = trailImage.classifications?.topResult?.type?.let { getLocationTypeString(it) }
            Text(text = stringResource(R.string.detected) + ": $topResultType")

            if (isExpanded) {
                Text(text = stringResource(R.string.accuracy) + ": ${trailImage.classifications?.topResult?.accuracy}")

                Spacer(modifier = Modifier.height(Dimens.separator))
                Text(
                    text = stringResource(R.string.other_detections),
                    fontWeight = FontWeight.Bold
                )

                trailImage.classifications?.otherResults?.forEach {
                    Text(text = getLocationTypeString(it.type) + ": ${it.accuracy}")
                }
            }
        }
    }
}

@Composable
private fun ImageViewer(
    trailImage: TrailImage,
    constraints: Constraints,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    //var rotation by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 20f)

        //rotation += rotationChange

        // to avoid overflow when zooming
        val extraWidth = (scale - 1) * constraints.maxWidth
        val extraHeight = (scale - 1) * constraints.maxHeight

        val maxX = extraWidth / 2
        val maxY = extraHeight / 2

        // calculates the offset
        offset = Offset(
            x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
        )
    }

    val transformableModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            //rotationZ = rotation
            translationX = offset.x
            translationY = offset.y
        }
        .transformable(transformableState)

    if (trailImage.imageUrl.isNotEmpty()) {
        SubcomposeAsyncImage(
            model = trailImage.imageUrl,
            contentDescription = null,
            loading = { CircularProgressIndicator() },
            modifier = transformableModifier
        )
    } else {
        Image(
            bitmap = trailImage.image.asImageBitmap(),
            contentDescription = null,
            modifier = transformableModifier
        )
    }
}

@Composable
private fun ButtonsRow(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // delete button
        FilledTonalButton(
            onClick = { onDelete() },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(text = stringResource(R.string.delete))
        }

        // close button
        Button(onClick = { onDismiss() }) {
            Text(text = stringResource(R.string.close))
        }
    }
}