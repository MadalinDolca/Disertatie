package com.madalin.disertatie.auth.presentation.password_reset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.madalin.disertatie.R
import com.madalin.disertatie.auth.presentation.actions.PasswordResetAction
import com.madalin.disertatie.auth.presentation.components.EmailField
import com.madalin.disertatie.core.presentation.components.AppClickableText
import com.madalin.disertatie.core.presentation.components.AppFilledButton
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasswordResetScreen(
    viewModel: PasswordResetViewModel = koinViewModel(),
    onNavigateToLoginClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        var email by rememberSaveable { mutableStateOf("") }
        val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_password_reset))

        LottieAnimation(
            composition = lottieComposition,
            modifier = Modifier
                .size(Dimens.lottieSize)
                .statusBarsPadding()
        )
        EmailField(
            value = email,
            onChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.emailError != UiText.Empty,
            errorMessage = uiState.emailError.asString(),
            placeholder = stringResource(R.string.email_address)
        )
        AppFilledButton(
            onClick = { viewModel.handleAction(PasswordResetAction.ResetPassword(email)) },
            text = stringResource(R.string.reset_your_password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(Dimens.separator))

        AppClickableText(
            text = stringResource(R.string.go_back_to_login),
            onClick = { onNavigateToLoginClick() },
            modifier = Modifier.navigationBarsPadding()
        )
    }

    StatusBanner(
        isVisible = uiState.isStatusBannerVisible,
        data = uiState.statusBannerData,
        onDismiss = { viewModel.handleAction(PasswordResetAction.HideStatusBanner) }
    )
}