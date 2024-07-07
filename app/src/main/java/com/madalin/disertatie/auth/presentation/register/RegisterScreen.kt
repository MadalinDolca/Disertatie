package com.madalin.disertatie.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.madalin.disertatie.auth.presentation.actions.RegisterAction
import com.madalin.disertatie.auth.presentation.components.EmailField
import com.madalin.disertatie.auth.presentation.components.PasswordField
import com.madalin.disertatie.core.presentation.components.AppClickableText
import com.madalin.disertatie.core.presentation.components.AppFilledButton
import com.madalin.disertatie.core.presentation.components.StatusBanner
import com.madalin.disertatie.core.presentation.components.StatusDialog
import com.madalin.disertatie.core.presentation.util.Dimens
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

            // resets the status so that the user will still be able to return to the register screen
            viewModel.handleAction(RegisterAction.ResetRegistrationStatus)
        }
    }

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
        var password by rememberSaveable { mutableStateOf("") }
        var confirmPassword by rememberSaveable { mutableStateOf("") }
        val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie_register))

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
            errorMessage = uiState.emailError.asString()
        )
        PasswordField(
            value = password,
            onChange = { password = it },
            submitAction = null,
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.passwordError != UiText.Empty,
            errorMessage = uiState.passwordError.asString()
        )
        PasswordField(
            value = confirmPassword,
            onChange = { confirmPassword = it },
            submitAction = { viewModel.handleAction(RegisterAction.DoRegistration(email, password, confirmPassword)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.confirm_password),
            isError = uiState.confirmPasswordError != UiText.Empty,
            errorMessage = uiState.confirmPasswordError.asString()
        )
        AppFilledButton(
            onClick = { viewModel.handleAction(RegisterAction.DoRegistration(email, password, confirmPassword)) },
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
        onDismiss = { viewModel.handleAction(RegisterAction.HideStatusBanner) }
    )
    StatusDialog(
        isVisible = uiState.isStatusDialogVisible,
        data = uiState.statusDialogData,
        onDismiss = { viewModel.handleAction(RegisterAction.HideStatusDialog) }
    )
}