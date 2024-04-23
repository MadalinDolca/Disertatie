package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.util.UiText

data class StatusDialogData(
    val type: StatusDialogType,
    val message: UiText
)

/**
 * Types supported by the [StatusDialog].
 */
sealed class StatusDialogType {
    data object Processing : StatusDialogType()
    data object Success : StatusDialogType()
    data object Failure : StatusDialogType()
}

/**
 * Dialog used to display [processing][StatusDialogType.Processing], [success][StatusDialogType.Success]
 * and [failure][StatusDialogType] messages.
 * @param isVisible `true` if the dialog should be visible, `false` otherwise
 * @param data [type][StatusDialogData.type] and [message][StatusDialogData.message] to display
 * @param type the [StatusDialogType] animations to display
 * @param message the message
 * @param onDismiss action to do when the user dismisses the dialog
 * @param modifier the [Modifier] to be applied to this dialog
 */
@Composable
fun StatusDialog(
    isVisible: Boolean,
    data: StatusDialogData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = modifier.padding(Dimens.container),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.container),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (data.type) {
                        StatusDialogType.Processing -> CircularProgressIndicator()

                        StatusDialogType.Success -> {
                            val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_success))
                            LottieAnimation(
                                composition = lottieComposition,
                                modifier = Modifier.size(Dimens.lottieSize)
                            )
                        }

                        StatusDialogType.Failure -> {
                            val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_failure))
                            LottieAnimation(
                                composition = lottieComposition,
                                modifier = Modifier.size(Dimens.lottieSize)
                            )

                        }
                    }
                    Spacer(modifier = Modifier.size(Dimens.separator))
                    Text(text = data.message.asString())
                }

            }
        }
    }
}

@LightDarkPreview
@Composable
private fun AppDialogPreview() {
    DisertatieTheme {
        Surface {
            StatusDialog(
                isVisible = true,
                data = StatusDialogData(StatusDialogType.Failure, UiText.Dynamic("Dummy message")),
                onDismiss = {}
            )
        }
    }
}