package com.madalin.disertatie.map.presentation

data class SuggestionDialogState(
    val isImagesChecked: Boolean = false,
    val isNoteChecked: Boolean = false,
    val isWeatherChecked: Boolean = false,
    val isWarningChecked: Boolean = false,
    val isTimeChecked: Boolean = false,
    val additionalInfo: String = "",
    val response: String = ""
)