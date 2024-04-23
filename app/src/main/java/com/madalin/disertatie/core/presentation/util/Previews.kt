package com.madalin.disertatie.core.presentation.util

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO, apiLevel = 28
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES, apiLevel = 28
)
annotation class LightDarkPreview

@Preview(
    name = "Dynamic Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dynamic Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
annotation class LightDarkDynamicPreview