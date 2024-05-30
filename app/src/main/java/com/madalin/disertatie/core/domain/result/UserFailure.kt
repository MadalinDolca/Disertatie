package com.madalin.disertatie.core.domain.result

/**
 * User data fetching failure types.
 */
sealed class UserFailure {
    data object NoUserId : UserFailure()
    data object UserDataNotFound : UserFailure()
    data object DataFetchingError : UserFailure()
}