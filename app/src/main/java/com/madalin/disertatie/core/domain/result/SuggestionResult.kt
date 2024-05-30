package com.madalin.disertatie.core.domain.result

/**
 * Represents the result of an AI suggestion request.
 */
sealed class SuggestionResult {
    data object Loading : SuggestionResult()
    data class Success(val response: String) : SuggestionResult()
    data object Error : SuggestionResult()
}