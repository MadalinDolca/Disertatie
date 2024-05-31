package com.madalin.disertatie.map.presentation.action

import android.content.Context
import com.madalin.disertatie.core.domain.action.Action

/**
 * Represents a location related action that can be performed.
 */
sealed class LocationAction : Action {
    data class SettingResultEnabled(val context: Context) : LocationAction()
    data object SettingResultDisabled : LocationAction()
}