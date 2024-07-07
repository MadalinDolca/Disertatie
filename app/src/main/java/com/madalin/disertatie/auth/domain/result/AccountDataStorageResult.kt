package com.madalin.disertatie.auth.domain.result

sealed class AccountDataStorageResult {
    data object Success : AccountDataStorageResult()
    data class Error(val message: String?) : AccountDataStorageResult()
}