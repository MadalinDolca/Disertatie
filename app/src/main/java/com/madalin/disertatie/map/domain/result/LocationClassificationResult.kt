package com.madalin.disertatie.map.domain.result

import com.madalin.disertatie.core.domain.model.LocationClassifications

/**
 * Represents the result of a location classification.
 */
sealed class LocationClassificationResult {
    data object Loading : LocationClassificationResult()
    data class Success(val data: LocationClassifications) : LocationClassificationResult()
    data class Error(val message: String) : LocationClassificationResult()
}