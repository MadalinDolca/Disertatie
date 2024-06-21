package com.madalin.disertatie.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens

enum class StatusPlaceholderType {
    SEARCHING,
    ERROR,
    EMPTY
}

@Composable
fun StatusPlaceholder(
    type: StatusPlaceholderType,
    modifier: Modifier = Modifier,
    text: String? = null
) {
    val resId = when (type) {
        StatusPlaceholderType.SEARCHING -> R.raw.lottie_search
        StatusPlaceholderType.ERROR -> R.raw.lottie_failure
        StatusPlaceholderType.EMPTY -> R.raw.lottie_lost
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId))
        LottieAnimation(
            composition = lottieComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(Dimens.lottieSize)
        )
        Spacer(modifier = Modifier.height(Dimens.separator))
        text?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.container)
            )
        }
    }
}