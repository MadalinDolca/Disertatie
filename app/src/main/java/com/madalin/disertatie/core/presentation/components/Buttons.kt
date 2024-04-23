package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens

@Composable
fun AppFilledButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(vertical = 15.dp)
    ) {
        Text(
            text = text,
            fontSize = Dimens.fontSizeRegular
        )
    }
}

@LightDarkPreview
@Composable
private fun AppButtonPreview() {
    DisertatieTheme {
        Surface {
            AppFilledButton(
                onClick = {},
                text = "Filled button",
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            fontSize = Dimens.fontSizeRegular
        )
    }
}

@LightDarkPreview
@Composable
fun AppTextButtonPreview() {
    DisertatieTheme {
        Surface {
            AppTextButton(
                onClick = {},
                text = "Text button"
            )
        }
    }
}