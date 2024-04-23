package com.madalin.disertatie.auth.presentation.login

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
import com.madalin.disertatie.auth.presentation.components.EmailField
import com.madalin.disertatie.auth.presentation.components.PasswordField
import com.madalin.disertatie.core.presentation.components.AppClickableText
import com.madalin.disertatie.core.presentation.components.AppFilledButton
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.theme.DisertatieTheme
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.LightDarkPreview
import com.madalin.disertatie.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreenRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToRegisterClick: () -> Unit,
    onNavigateToPasswordResetClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        uiState = uiState,
        onSetLoginOperationStatus = { viewModel.setLoginOperationStatus(false) }, // set to false so that the use will still be able to return to the login screen
        onToggleStatusBannerVisibility = { viewModel.toggleStatusBannerVisibility(false) },
        onSetEmail = viewModel::setEmail,
        onSetPassword = viewModel::setPassword,
        onLogin = viewModel::login,
        onNavigateToRegisterClick = onNavigateToRegisterClick,
        onNavigateToPasswordResetClick = onNavigateToPasswordResetClick
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSetLoginOperationStatus: () -> Unit,
    onToggleStatusBannerVisibility: () -> Unit,
    onSetEmail: (String) -> Unit,
    onSetPassword: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onNavigateToRegisterClick: () -> Unit,
    onNavigateToPasswordResetClick: () -> Unit
) {
    LaunchedEffect(uiState.isLoginOperationComplete) {
        if (uiState.isLoginOperationComplete) {
            onSetLoginOperationStatus()
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
            val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_login))
            LottieAnimation(
                composition = lottieComposition,
                modifier = Modifier
                    .size(Dimens.lottieSize)
                    .statusBarsPadding()
            )

            EmailField(
                value = uiState.email,
                onChange = { onSetEmail(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(id = R.string.email),
                isError = uiState.emailError != UiText.Empty,
                errorMessage = uiState.emailError.asString()
            )

            PasswordField(
                value = uiState.password,
                onChange = { onSetPassword(it) },
                submitAction = { onLogin(uiState.email, uiState.password) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.passwordError != UiText.Empty,
                errorMessage = uiState.passwordError.asString()
            )

            AppFilledButton(
                onClick = { onLogin(uiState.email, uiState.password) },
                text = stringResource(id = R.string.login),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(Dimens.separator))

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.separator)) {
                Text(stringResource(R.string.dont_have_an_account))

                AppClickableText(
                    text = stringResource(R.string.sign_up),
                    onClick = { onNavigateToRegisterClick() }
                )
            }

            AppClickableText(
                text = stringResource(R.string.recover_your_account),
                onClick = { onNavigateToPasswordResetClick() },
                modifier = Modifier.navigationBarsPadding()
            )
        }

        StatusBanner(
            isVisible = uiState.isStatusBannerVisible,
            data = uiState.statusBannerData,
            onDismiss = { onToggleStatusBannerVisibility() }
        )
    }
}

@LightDarkPreview
@Composable
private fun LoginScreenPreview() {
    DisertatieTheme {
        Surface {
            LoginScreen(
                uiState = LoginUiState(),
                onSetLoginOperationStatus = {},
                onToggleStatusBannerVisibility = {},
                onSetEmail = {},
                onSetPassword = {},
                onLogin = { _: String, _: String -> },
                onNavigateToRegisterClick = {},
                onNavigateToPasswordResetClick = {}
            )
        }
    }
}