package com.madalin.disertatie.map.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.map.presentation.action.TrailAction

@Composable
fun TrailEndDialog(
    isVisible: Boolean,
    isUploading: Boolean,
    trail: Trail,
    trailNameError: UiText,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(modifier = modifier.padding(Dimens.container)) {
                if (!isUploading) {
                    TrailForm(
                        trailName = trail.name,
                        trailNameError = trailNameError,
                        trailDescription = trail.description,
                        isTrailPublic = trail.public,
                        onAction = onAction
                    )
                } else {
                    UploadingIndicator(
                        onCancelClick = { onAction(TrailAction.CancelSavingTrail) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.container)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailForm(
    trailName: String,
    trailNameError: UiText,
    trailDescription: String,
    isTrailPublic: Boolean,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(Dimens.container)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.separator)
    ) {
        Text(
            text = stringResource(R.string.add_info_about_this_trail),
            style = MaterialTheme.typography.titleLarge
        )
        // trail name
        OutlinedTextField(
            value = trailName,
            onValueChange = { onAction(TrailAction.UpdateTrailName(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.add_trail_name)) },
            supportingText = {
                if (trailNameError != UiText.Empty) {
                    Text(
                        text = trailNameError.asString(),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = trailNameError != UiText.Empty,
            maxLines = 3,
            shape = MaterialTheme.shapes.medium
        )
        // trail description
        OutlinedTextField(
            value = trailDescription,
            onValueChange = { onAction(TrailAction.UpdateTrailDescription(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.add_trail_description)) },
            maxLines = 5,
            shape = MaterialTheme.shapes.medium
        )
        // visibility switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.make_trail_public))
            Switch(
                checked = isTrailPublic,
                onCheckedChange = { onAction(TrailAction.SetTrailVisibility(it)) }
            )
        }
        // buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onAction(TrailAction.DontSaveTrail) }) {
                Text(text = stringResource(R.string.dont_save))
            }
            Button(onClick = { onAction(TrailAction.SaveTrail) }) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun UploadingIndicator(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Dimens.separator))

        Text(text = stringResource(R.string.uploading_trail))
        Spacer(modifier = Modifier.height(Dimens.separator))

        TextButton(onClick = { onCancelClick() }) {
            Text(text = stringResource(R.string.cancel))
        }
    }
}