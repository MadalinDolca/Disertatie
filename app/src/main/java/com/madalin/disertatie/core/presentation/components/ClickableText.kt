package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme

@Composable
fun AppClickableText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        color = color,
        modifier = modifier.clickable {
            onClick()
        }
    )
}

@LightDarkPreview
@Composable
fun AppClickableTextPreview() {
    DisertatieTheme {
        Surface {
            AppClickableText(text = "This is a text", onClick = {})
        }
    }
}