package com.madalin.disertatie.core.domain.failure

/**
 * User data fetching failure types.
 */
sealed class UserFailure {
    data object NoUserId : UserFailure()
    data object UserDataNotFound : UserFailure()
    data object DataFetchingError : UserFailure()
}