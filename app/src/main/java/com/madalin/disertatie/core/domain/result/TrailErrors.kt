package com.madalin.disertatie.core.domain.result

sealed class TrailInfoError {
    data object NotFound : TrailInfoError()
    data class Error(val message: String?) : TrailInfoError()
}