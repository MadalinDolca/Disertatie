package com.madalin.disertatie.map.presentation.action

import com.madalin.disertatie.core.domain.action.Action

sealed class SuggestionAction : Action {
    // suggestion generation
    data object GetActivitySuggestions : SuggestionAction()

    // suggestion dialog
    data object ShowSuggestionDialog : SuggestionAction()
    data object HideSuggestionDialog : SuggestionAction()

    // suggestion dialog state
    data class SetImageState(val isChecked: Boolean) : SuggestionAction()
    data class SetOnlyClassificationsState(val isChecked: Boolean) : SuggestionAction()
    data class SetNoteState(val isChecked: Boolean) : SuggestionAction()
    data class SetWeatherState(val isChecked: Boolean) : SuggestionAction()
    data class SetWarningState(val isChecked: Boolean) : SuggestionAction()
    data class SetTimeState(val isChecked: Boolean) : SuggestionAction()
    data class SetAdditionalInfo(val info: String) : SuggestionAction()
    data object CopySuggestion : SuggestionAction()
}