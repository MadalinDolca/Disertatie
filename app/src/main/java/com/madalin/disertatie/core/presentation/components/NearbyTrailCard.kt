package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwipeLeftAlt
import androidx.compose.material.icons.rounded.SwipeRightAlt
import androidx.compose.material.icons.rounded.SwitchLeft
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.madalin.disertatie.core.domain.extension.prettyLength
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.util.Dimens

@Composable
fun NearbyTrailCard(
    trail: Trail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick() },
        modifier = modifier.width(280.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.container)) {
            Text(
                text = trail.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            if (trail.description.isNotEmpty()) {
                Text(
                    text = trail.description,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                Column {
                    trail.distanceToStartingPoint?.let {
                        DataRow(icon = Icons.Rounded.SwipeRightAlt, text = it.prettyLength())
                    }
                    trail.distanceToEndingPoint?.let {
                        DataRow(icon = Icons.Rounded.SwipeLeftAlt, text = it.prettyLength())
                    }
                }
                Spacer(modifier = Modifier.width(Dimens.separator))
                Column {
                    trail.distanceToMiddlePoint?.let {
                        DataRow(icon = Icons.Rounded.SwitchLeft, text = it.prettyLength())
                    }
                    trail.length?.let {
                        DataRow(icon = Icons.Rounded.Straighten, text = it.prettyLength())
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text)
    }
}