package com.madalin.disertatie.core.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.asDate
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.util.Dimens

@Composable
fun TrailBannerItem(
    trail: Trail,
    currentUserId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            BackgroundImage(image = null)

            Row(modifier = Modifier.padding(Dimens.container)) {
                TextContent(
                    trail = trail,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Dimens.separator))

                SideIcons(
                    trailCreatorId = trail.userId,
                    currentUserId = currentUserId,
                    isPublic = trail.public
                )
            }
        }
    }
}

@Composable
private fun TextContent(
    trail: Trail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = trail.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = trail.description,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(Dimens.separator))

        val creationDate = trail.createdAt?.time?.asDate()
        if (creationDate != null) {
            Text(
                text = stringResource(R.string.publised_at) + " " + creationDate,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SideIcons(
    trailCreatorId: String,
    currentUserId: String,
    isPublic: Boolean,
    modifier: Modifier = Modifier
) {
    if (currentUserId == trailCreatorId) {
        Column(modifier = modifier.fillMaxHeight()) {
            Icon(
                imageVector = if (isPublic) Icons.Rounded.Public
                else Icons.Rounded.Lock,
                contentDescription = "Visibility"
            )
            Spacer(modifier = Modifier.height(Dimens.separator))
            Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
private fun BackgroundImage(
    image: Bitmap?,
    modifier: Modifier = Modifier
) {
    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxHeight()
                .graphicsLayer { alpha = 0.5f }
                .drawWithContent {
                    val colors = listOf(Color.Black, Color.Transparent)
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(colors),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
    }
}