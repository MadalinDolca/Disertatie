package com.madalin.disertatie.auth.domain.result

import com.google.firebase.auth.FirebaseUser

sealed class RegisterResult {
    data class Success(val firebaseUser: FirebaseUser?) : RegisterResult()
    data object InvalidEmail : RegisterResult()
    data object InvalidCredentials : RegisterResult()
    data object EmailIsTaken : RegisterResult()
    data class Error(val message: String?) : RegisterResult()
}