package com.madalin.disertatie.core.domain.result

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint

sealed class TrailsListResult {
    data class Success(val trails: List<Trail>) : TrailsListResult()
    data class Error(val error: String?) : TrailsListResult()
}

sealed class TrailInfoResult {
    data class Success(val trail: Trail) : TrailInfoResult()
    data object NotFound : TrailInfoResult()
    data class Error(val error: String?) : TrailInfoResult()
}

sealed class TrailPointsResult {
    data class Success(val points: List<TrailPoint>) : TrailPointsResult()
    data class Error(val error: String?) : TrailPointsResult()
}

sealed class TrailImagesResult {
    data class Success(val images: List<String>) : TrailImagesResult()
    data class Error(val error: String?) : TrailImagesResult()
}

sealed class TrailUpdateResult {
    data object Success : TrailUpdateResult()
    data class Error(val error: String?) : TrailUpdateResult()
}

sealed class TrailDeleteResult {
    data object Success : TrailDeleteResult()
    data class Error(val error: String?) : TrailDeleteResult()
}