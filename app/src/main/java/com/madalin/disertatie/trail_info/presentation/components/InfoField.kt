package com.madalin.disertatie.trail_info.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.jeziellago.compose.markdowntext.MarkdownText

enum class InfoFieldType {
    NAME, DESCRIPTION
}

@Composable
fun InfoField(
    type: InfoFieldType,
    text: String,
    placeholder: @Composable () -> Unit,
    isEditing: Boolean,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditing) {
        OutlinedTextField(
            value = text,
            onValueChange = { onAction(it) },
            placeholder = { placeholder() },
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        )
    } else {
        if (type == InfoFieldType.NAME) {
            Text(
                text = text,
                modifier = modifier,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        } else {
            MarkdownText(
                markdown = text,
                modifier = modifier
            )
        }
    }
}