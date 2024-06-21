package com.madalin.disertatie.trail_info.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.prettyLength
import com.madalin.disertatie.core.domain.model.TrailImage
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.presentation.util.Dimens
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun TrailPointBannerItem(
    trailPoint: TrailPoint,
    distance: Float,
    bottomSpacing: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .animateContentSize()
            .height(IntrinsicSize.Min)
    ) {
        // distance column
        Column(
            modifier = Modifier.padding(horizontal = Dimens.container),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Hiking,
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(5.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = distance.prettyLength(),
                style = MaterialTheme.typography.labelSmall
            )
            DashedVerticalLine(

            )
        }
        // info column
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.separator)) {
            if (trailPoint.imagesList.isNotEmpty()) {
                ImagesRow(imagesList = trailPoint.imagesList)
            }
            if (trailPoint.hasWarning) {
                WarningAlert()
            }
            if (trailPoint.note.isNotEmpty()) {
                var isExpanded by remember { mutableStateOf(false) }
                NotesCard(
                    text = trailPoint.note,
                    isExpanded = isExpanded,
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.padding(end = Dimens.container)
                )
            }
            Spacer(modifier = Modifier.height(bottomSpacing))
        }
    }
}

@Composable
private fun DashedVerticalLine(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.secondary,
    pathEffect: PathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
) {
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
    ) {
        drawLine(
            color = color,
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, size.height),
            strokeWidth = thickness.toPx(),
            cap = StrokeCap.Butt,
            pathEffect = pathEffect
        )
    }
}

@Composable
private fun ImagesRow(
    imagesList: List<TrailImage>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space = Dimens.separator)
    ) {
        imagesList.forEachIndexed { index, trailImage ->
            Image(
                painter = rememberAsyncImagePainter(model = trailImage.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = if (index == imagesList.lastIndex) Dimens.container else 0.dp)
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}

@Composable
private fun WarningAlert(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(Dimens.separator))
        Text(
            text = stringResource(R.string.be_careful_here),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun NotesCard(
    text: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        MarkdownText(
            markdown = text,
            modifier = Modifier.padding(Dimens.container),
            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
            onClick = { onClick() },
            truncateOnTextOverflow = true
        )
    }
}