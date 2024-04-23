package com.madalin.disertatie.auth.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.auth.domain.failures.LoginFailure
import com.madalin.disertatie.auth.domain.failures.RegisterFailure
import com.madalin.disertatie.core.domain.entity.User

/**
 * Repository interface that contains authentication related methods for [Firebase].
 */
interface FirebaseAuthRepository {
    /**
     * Signs up the user with the given [email] and [password] and store their data to [Firestore][Firebase.firestore].
     * @param onSuccess callback function that will be invoked when the registration process succeeded
     * - [FirebaseUser] parameter contains the stored user's profile information in Firebase
     * @param onFailure callback function that will be invoked when the registration process failed
     * - [SignUpFailure] parameter contains the failure type
     */
    fun createUserWithEmailAndPassword(
        email: String, password: String,
        onSuccess: (FirebaseUser?) -> Unit, onFailure: (RegisterFailure) -> Unit
    )

    /**
     * Stores the user data into Firestore.
     * @param onSuccess callback function that will be invoked when the storage process succeeded
     * @param onFailure callback function that will be invoked when the storage process failed
     * - [String] parameter contains the error message
     */
    fun storeAccountDataToFirestore(user: User, onSuccess: () -> Unit, onFailure: (String?) -> Unit)

    /**
     * Resets the user password associated with the given [email].
     * @param onSuccess callback function that will be invoked when the reset process succeeded
     * @param onFailure callback function that will be invoked when the reset process failed
     */
    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: () -> Unit)

    /**
     * Signs in the user with the given [email] and [password].
     * @param onSuccess callback function that will be invoked when the authentication process succeeded
     * @param onFailure callback function that will be invoked when the authentication process failed
     * - [SignInFailure] parameter contains the failure type
     */
    fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccess: () -> Unit, onFailure: (LoginFailure) -> Unit
    )

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