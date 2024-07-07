package com.madalin.disertatie.auth.domain.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.auth.domain.result.AccountDataStorageResult
import com.madalin.disertatie.auth.domain.result.LoginResult
import com.madalin.disertatie.auth.domain.result.PasswordResetResult
import com.madalin.disertatie.auth.domain.result.RegisterResult
import com.madalin.disertatie.core.domain.model.User

/**
 * Repository interface that contains authentication related methods for [Firebase].
 */
interface FirebaseAuthRepository {
    /**
     * Signs up the user with the given [email] and [password] and stores their data to
     * [Firestore][Firebase.firestore]. If the operation succeeds, it returns the stored user's
     * profile information in Firebase as a [RegisterResult].
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): RegisterResult

    /**
     * Stores the given [user] data into Firestore and returns an [AccountDataStorageResult].
     */
    suspend fun storeAccountDataToFirestore(user: User): AccountDataStorageResult

    /**
     * Resets the user password associated with the given [email] and returns a [PasswordResetResult].
     */
    suspend fun resetPassword(email: String): PasswordResetResult

    /**
     * Signs in the user with the given [email] and [password].
     * @param onSuccess callback function that will be invoked when the authentication process succeeded
     * @param onFailure callback function that will be invoked when the authentication process failed
     * - [SignInFailure] parameter contains the failure type
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): LoginResult

    /**
     * Sends an email verification to the currently logged in account email.
     */
    fun sendEmailVerification()

    /**
     * Checks if the current Firebase user email is verified.
     * @return `true` if verified, `false` otherwise
     */
    fun isEmailVerified(): Boolean
}