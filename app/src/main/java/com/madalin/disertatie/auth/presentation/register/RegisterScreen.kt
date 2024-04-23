package com.madalin.disertatie.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.data.MockFirebaseAuthRepositoryImpl
import com.madalin.disertatie.auth.presentation.components.EmailField
import com.madalin.disertatie.auth.presentation.components.PasswordField
import com.madalin.disertatie.core.presentation.components.AppClickableText
import com.madalin.disertatie.core.presentation.components.AppFilledButton
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.components.StatusDialog
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onNavigateToLoginClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isRegisterOperationComplete) {
        if (uiState.isRegisterOperationComplete) {
            onNavigateToLoginClick()
            viewModel.setRegisterOperationStatus(false) // set to false so that the user will still be able to return to the register screen
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.container)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(
                space = Dimens.separator,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_register))
            LottieAnimation(
                composition = lottieComposition,
                modifier = Modifier
                    .size(Dimens.lottieSize)
                    .statusBarsPadding()
            )

            EmailField(
                value = uiState.email,
                onChange = { viewModel.setEmail(it) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.emailError != UiText.Empty,
                errorMessage = uiState.emailError.asString()
            )

            PasswordField(
                value = uiState.password,
                onChange = { viewModel.setPassword(it) },
                submitAction = null,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.passwordError != UiText.Empty,
                errorMessage = uiState.passwordError.asString()
            )

            PasswordField(
                value = uiState.confirmPassword,
                onChange = { viewModel.setConfirmPassword(it) },
                submitAction = { viewModel.register(uiState.email, uiState.password, uiState.confirmPassword) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.confirm_password),
                isError = uiState.confirmPasswordError != UiText.Empty,
                errorMessage = uiState.confirmPasswordError.asString()
            )

            AppFilledButton(
                onClick = { viewModel.register(uiState.email, uiState.password, uiState.confirmPassword) },
                text = stringResource(R.string.create_account),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(Dimens.separator))

            Row(
                modifier = Modifier.navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.separator)
            ) {
                Text(stringResource(R.string.already_have_an_account))

                AppClickableText(
                    text = stringResource(R.string.login),
                    onClick = { onNavigateToLoginClick() }
                )
            }
        }

        StatusBanner(
            isVisible = uiState.isStatusBannerVisible,
            data = uiState.statusBannerData,
            onDismiss = { viewModel.toggleStatusBannerVisibility(false) }
        )

        StatusDialog(
            isVisible = uiState.isStatusDialogVisible,
            data = uiState.statusDialogData,
            onDismiss = { viewModel.toggleStatusDialogVisibility(false) }
        )
    }
}

@LightDarkPreview
@Composable
private fun RegisterScreenPreview() {
    DisertatieTheme {
        Surface {
            RegisterScreen(
                viewModel = RegisterViewModel(MockFirebaseAuthRepositoryImpl),
                onNavigateToLoginClick = {}
            )
        }
    }
}