package com.madalin.disertatie.core.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Represents different types of strings, including dynamically provided strings,
 * empty strings, and strings resources identified by a resource ID.
 */
sealed class UiText {
    /**
     * Represents a dynamically provided string [value].
     */
    data class Dynamic(val value: String) : UiText()

    /**
     * Represents a string resource identified by [resId] and optional [arguments][args] for
     * string formatting.
     */
    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    /**
     * Represents an empty string.
     */
    data object Empty : UiText()

    /**
     * Converts this [UiText] to a standard [String] representation and returns it.
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> stringResource(resId, *args)
            Empty -> ""
        }
    }

    /**
     * Converts this [UiText] to a standard [String] representation and returns it.
     * @param context the [Context] used for retrieving string resources
     */
    fun asString(context: Context): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> context.getString(resId, *args)
            Empty -> ""
        }
    }
}