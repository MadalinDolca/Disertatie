package com.madalin.disertatie.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme

/**
 * Text field used for email address.
 * @param value the email address
 * @param onChange what to do when the field value changes
 * @param modifier the [Modifier] to be applied to this field
 * @param placeholder placeholder to be displayed when the text field is in focus and the input text is empty
 * @param isError `true` if there is an error
 * @param errorMessage message to display when [isError]
 */
@Composable
fun EmailField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.email),
    isError: Boolean = false,
    errorMessage: String
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        placeholder = { Text(text = placeholder) },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        trailingIcon = {
            if (isError) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        visualTransformation = VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@LightDarkPreview
@Composable
private fun EmailFieldPreview() {
    DisertatieTheme {
        Surface {
            EmailField(
                value = "email@email.com",
                onChange = {},
                isError = true,
                errorMessage = "This is an error"
            )
        }
    }
}

/**
 * Text field used for passwords that has a button to toggle password visibility.
 * @param value the password
 * @param onChange what to do when the field value changes
 * @param modifier the [Modifier] to be applied to this field
 * @param placeholder placeholder to be displayed when the text field is in focus and the input text is empty
 * @param submitAction action to perform when the user triggers the action button. If `null` the
 * focus will be moved down.
 * @param isError `true` if there is an error
 * @param errorMessage message to display when [isError]
 */
@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    submitAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.password),
    isError: Boolean = false,
    errorMessage: String
) {
    val focusManager = LocalFocusManager.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    val trailingIcon = @Composable {
        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
            Icon(
                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = stringResource(R.string.toggle_visibility),
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        placeholder = { Text(text = placeholder) },
        trailingIcon = { if (value.isNotEmpty()) trailingIcon() },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (submitAction != null) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onDone = { if (submitAction != null) submitAction() },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@LightDarkPreview
@Composable
private fun PasswordFieldPreview() {
    DisertatieTheme {
        Surface {
            PasswordField(
                value = "mypassword",
                onChange = { },
                submitAction = { },
                isError = true,
                errorMessage = "This is an error"
            )
        }
    }
}