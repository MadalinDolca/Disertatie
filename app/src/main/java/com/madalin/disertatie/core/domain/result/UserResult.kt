package com.madalin.disertatie.core.domain.result

import com.madalin.disertatie.core.domain.model.User

sealed class UserResult {
    data class Success(val user: User) : UserResult()
    data object NoUserId : UserResult()
    data object UserDataNotFound : UserResult()
    data object DataFetchingError : UserResult()
}