package com.madalin.disertatie.map.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.action.Action
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.map.presentation.SuggestionDialogState
import com.madalin.disertatie.map.presentation.action.SuggestionAction
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ActivitySuggestionsDialog(
    isVisible: Boolean,
    suggestionDialogState: SuggestionDialogState,
    isSuggestionLoading: Boolean,
    trailPoint: TrailPoint,
    onDismiss: () -> Unit,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(modifier = modifier.padding(Dimens.container)) {
                Column(modifier = Modifier.padding(Dimens.container)) {
                    if (isSuggestionLoading) {
                        LoadingSuggestionIndicator()
                    } else if (suggestionDialogState.response.isNotEmpty()) {
                        SuggestionResult(
                            suggestion = suggestionDialogState.response,
                            onDismiss = { onDismiss() },
                            onCopySuggestion = { onAction(SuggestionAction.CopySuggestion) }
                        )
                    } else {
                        SuggestionSettings(
                            suggestionDialogState = suggestionDialogState,
                            trailPoint = trailPoint,
                            onDismiss = { onDismiss() },
                            onAction = onAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionSettings(
    suggestionDialogState: SuggestionDialogState,
    trailPoint: TrailPoint,
    onDismiss: () -> Unit,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.ask_ai_for_activity_suggestions),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(Dimens.separator))

        Switches(
            suggestionDialogState = suggestionDialogState,
            trailPoint = trailPoint,
            onAction = onAction
        )

        Spacer(modifier = Modifier.height(Dimens.separator))

        OutlinedTextField(
            value = suggestionDialogState.additionalInfo,
            onValueChange = { onAction(SuggestionAction.SetAdditionalInfo(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.add_additional_info)) },
            maxLines = 5,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(Dimens.separator))

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.cancel))
            }
            Button(
                onClick = { onAction(SuggestionAction.GetActivitySuggestions) },
                enabled = isGetSuggestionsButtonEnabled(suggestionDialogState)
            ) {
                Text(text = stringResource(R.string.get_suggestions))
            }
        }
    }
}

@Composable
private fun Switches(
    suggestionDialogState: SuggestionDialogState,
    trailPoint: TrailPoint,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (trailPoint.imagesList.isNotEmpty()) {
            SwitchRow(
                text = stringResource(R.string.include_images),
                isChecked = suggestionDialogState.isImagesChecked,
                onCheckedChange = { onAction(SuggestionAction.SetImageState(it)) }
            )
        }

        if (trailPoint.note.isNotEmpty()) {
            SwitchRow(
                text = stringResource(R.string.include_note),
                isChecked = suggestionDialogState.isNoteChecked,
                onCheckedChange = { onAction(SuggestionAction.SetNoteState(it)) }
            )
        }

        if (trailPoint.weather != null) {
            SwitchRow(
                text = stringResource(R.string.include_weather),
                isChecked = suggestionDialogState.isWeatherChecked,
                onCheckedChange = { onAction(SuggestionAction.SetWeatherState(it)) }
            )
        }

        if (trailPoint.hasWarning) {
            SwitchRow(
                text = stringResource(R.string.include_warning),
                isChecked = suggestionDialogState.isWarningChecked,
                onCheckedChange = { onAction(SuggestionAction.SetWarningState(it)) }
            )
        }

        SwitchRow(
            text = stringResource(R.string.include_time),
            isChecked = suggestionDialogState.isTimeChecked,
            onCheckedChange = { onAction(SuggestionAction.SetTimeState(it)) }
        )
    }
}

@Composable
private fun SwitchRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text)
        Switch(checked = isChecked, onCheckedChange = { onCheckedChange(it) })
    }
}

@Composable
private fun LoadingSuggestionIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Dimens.separator))
        Text(text = stringResource(R.string.loading) + "...")
    }
}

@Composable
private fun SuggestionResult(
    suggestion: String,
    onDismiss: () -> Unit,
    onCopySuggestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.separator)
    ) {
        Text(
            text = stringResource(R.string.ai_suggested_the_following),
            style = MaterialTheme.typography.titleLarge
        )

        ElevatedCard {
            MarkdownText(
                markdown = suggestion,
                modifier = Modifier
                    .padding(Dimens.container),
                maxLines = 15
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.dismiss))
            }

            Button(onClick = {
                onCopySuggestion()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.copy_suggestion))
            }
        }
    }
}

private fun isGetSuggestionsButtonEnabled(state: SuggestionDialogState): Boolean {
    return !(!state.isImagesChecked
            && !state.isNoteChecked
            && !state.isWeatherChecked
            && !state.isWarningChecked
            && !state.isTimeChecked
            && state.additionalInfo.isEmpty())
}