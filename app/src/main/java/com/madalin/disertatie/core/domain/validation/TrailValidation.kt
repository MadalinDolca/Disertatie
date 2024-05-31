package com.madalin.disertatie.core.domain.validation

/**
 * Object used to validate trail data.
 */
object TrailValidator {
    const val MIN_TRAIL_NAME_LENGTH = 6
    const val MAX_TRAIL_NAME_LENGTH = 70

    fun validateName(name: String) = when {
        name.isEmpty() -> NameResult.NotProvided
        name.length < MIN_TRAIL_NAME_LENGTH -> NameResult.TooShort
        name.length > MAX_TRAIL_NAME_LENGTH -> NameResult.TooLong
        else -> NameResult.Valid
    }

    sealed class NameResult {
        data object Valid : NameResult()
        data object NotProvided : NameResult()
        data object TooShort : NameResult()
        data object TooLong : NameResult()
    }
}