package com.madalin.disertatie.auth.data

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.result.AccountDataStorageResult
import com.madalin.disertatie.auth.domain.result.LoginResult
import com.madalin.disertatie.auth.domain.result.PasswordResetResult
import com.madalin.disertatie.auth.domain.result.RegisterResult
import com.madalin.disertatie.core.data.CollectionPath
import com.madalin.disertatie.core.domain.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val externalScope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FirebaseAuthRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): RegisterResult {
        try {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
            return RegisterResult.Success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            return RegisterResult.InvalidEmail
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return RegisterResult.InvalidCredentials
        } catch (e: FirebaseAuthUserCollisionException) {
            return RegisterResult.EmailIsTaken
        } catch (e: Exception) {
            return RegisterResult.Error(e.message)
        }
    }

    override suspend fun storeAccountDataToFirestore(user: User): AccountDataStorageResult {
        val docRef = firestore.collection(CollectionPath.USERS).document(user.id)

        try {
            docRef.set(user).await()
            return AccountDataStorageResult.Success
        } catch (e: Exception) {
            return AccountDataStorageResult.Error(e.message)
        }
    }

    override suspend fun resetPassword(email: String): PasswordResetResult {
        try {
            auth.sendPasswordResetEmail(email).await()
            return PasswordResetResult.Success(email)
        } catch (e: Exception) {
            return PasswordResetResult.Error(e.message)
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): LoginResult {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            return LoginResult.Success
        } catch (e: FirebaseAuthInvalidUserException) {
            return LoginResult.UserNotFound
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return LoginResult.InvalidCredentials
        } catch (e: Exception) {
            return LoginResult.Error(e.message)
        }
    }

    override fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }

    override fun isEmailVerified() = auth.currentUser?.isEmailVerified == true
}