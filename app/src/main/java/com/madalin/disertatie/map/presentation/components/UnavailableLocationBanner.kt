package com.madalin.disertatie.map.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens

@Composable
fun UnavailableLocationBanner(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onEnableLocationClick: () -> Unit
) {
    val text = buildAnnotatedString {
        appendInlineContent("locationIconId", "[icon]")
        append(" ")
        append(stringResource(R.string.location_is_unavailable_it_might_be_disabled_tap_this_banner_to_enable_it))
    }
    val inlineContent = mapOf(
        Pair(
            "locationIconId",
            InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(imageVector = Icons.Rounded.LocationOff, contentDescription = null)
            }
        )
    )

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
        Card(
            onClick = { onEnableLocationClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = text,
                inlineContent = inlineContent,
                modifier = Modifier.padding(Dimens.container),
                textAlign = TextAlign.Center
            )
        }
    }
}