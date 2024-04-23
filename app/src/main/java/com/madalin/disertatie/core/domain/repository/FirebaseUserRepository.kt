package com.madalin.disertatie.core.domain.repository

import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.core.domain.entity.User
import com.madalin.disertatie.core.domain.failures.UserFailure

interface FirebaseUserRepository {
    /**
     * Checks if the current Firebase user is signed in.
     * @return `true` if signed in, `false` otherwise
     */
    fun isSignedIn(): Boolean

    /**
     * Obtains the current user [Firebase] ID and returns it if it's not null.
     */
    fun getCurrentUserId(): String?

    /**
     * Obtains the current user data from [Firestore][Firebase.firestore] and starts listening for updates.
     * @param onSuccess function invoked when data fetching succeeds with the data inside [User]
     * @param onFailure function invoked when data fetching fails with [UserFailure] as the error type
     */
    fun startListeningForUserData(onSuccess: (User) -> Unit, onFailure: (UserFailure) -> Unit)

    /**
     * Signs out the currently authenticated user.
     * @param onSuccess function invoked when sign-out operation succeeds
     * @param onFailure function invoked when sign-out operation fails
     * - `String` parameter contains the error message
     */
    fun signOut(onSuccess: () -> Unit, onFailure: (String?) -> Unit)
}