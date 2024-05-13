package com.madalin.disertatie.core.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.util.UiText
import kotlinx.coroutines.delay

data class StatusBannerData(
    val type: StatusBannerType,
    val message: UiText
)

sealed class StatusBannerType {
    data object Success : StatusBannerType()
    data object Error : StatusBannerType()
    data object Info : StatusBannerType()
}

@Composable
fun StatusBanner(
    isVisible: Boolean,
    data: StatusBannerData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible) {
        delay(3000)
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(Dimens.container)
                .statusBarsPadding(),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(Dimens.container),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (data.type) {
                        StatusBannerType.Success -> Icons.Rounded.CheckCircle
                        StatusBannerType.Error -> Icons.Rounded.Cancel
                        StatusBannerType.Info -> Icons.Rounded.Info
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(Dimens.separator))
                Text(text = data.message.asString())
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun BannerPreview() {
    DisertatieTheme {
        Surface {
            StatusBanner(
                isVisible = true,
                data = StatusBannerData(StatusBannerType.Info, UiText.Dynamic("This is a message")),
                onDismiss = {}
            )
        }
    }
}