package com.madalin.disertatie.map.domain.util

import com.madalin.disertatie.core.domain.extension.asTime
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.map.presentation.SuggestionDialogState

/**
 * Builds and returns a prompt to be used in a suggestion generation based on the settings made
 * made in the suggestion dialog and the information of the selected trail point.
 */
fun buildPrompt(selectedTrailPoint: TrailPoint?, dialogState: SuggestionDialogState): String {
    val temperatureText = if (dialogState.isWeatherChecked) {
        "\n- the weather is ${selectedTrailPoint?.weather?.weatherDescription}, " +
                "with a temperature of ${selectedTrailPoint?.weather?.mainTemperature} Degree Celsius, " +
                "and a wind speed of ${selectedTrailPoint?.weather?.windSpeed} m/s"
    } else ""

    val trailPointNoteText = if (dialogState.isNoteChecked) {
        "\n- the people said this about this place '${selectedTrailPoint?.note}'"
    } else ""

    val timeText = if (dialogState.isTimeChecked) {
        "\n- the time is ${selectedTrailPoint?.timestamp?.asTime()}"
    } else ""

    val warningText = if (dialogState.isWarningChecked) {
        "\n- it might be a dangerous place"
    } else ""

    val additionalInfoText = if (dialogState.additionalInfo.isNotEmpty()) {
        "\n- ${dialogState.additionalInfo}"
    } else ""

    return temperatureText + trailPointNoteText + timeText + warningText + additionalInfoText
}