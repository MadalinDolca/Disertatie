package com.madalin.disertatie.auth.data

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.domain.failures.LoginFailure
import com.madalin.disertatie.auth.domain.failures.RegisterFailure
import com.madalin.disertatie.core.data.CollectionPath
import com.madalin.disertatie.core.domain.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class FirebaseAuthRepositoryImpl(
    private val externalScope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FirebaseAuthRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override fun createUserWithEmailAndPassword(
        email: String, password: String,
        onSuccess: (FirebaseUser?) -> Unit, onFailure: (RegisterFailure) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke(auth.currentUser)
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> onFailure(RegisterFailure.InvalidEmail)
                        is FirebaseAuthInvalidCredentialsException -> onFailure(RegisterFailure.InvalidCredentials)
                        is FirebaseAuthUserCollisionException -> onFailure(RegisterFailure.EmailIsTaken)
                        else -> onFailure(RegisterFailure.Error(task.exception?.message))
                    }
                }
            }
    }

    override fun storeAccountDataToFirestore(
        user: User,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(CollectionPath.USERS)
            .document(user.id) // adds user data into the document with the user id as a name
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message)
                }
            }
    }

    override fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
    }

    override fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccess: () -> Unit, onFailure: (LoginFailure) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke()
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> onFailure(LoginFailure.UserNotFound)
                        is FirebaseAuthInvalidCredentialsException -> onFailure(LoginFailure.InvalidCredentials)
                        else -> onFailure(LoginFailure.Error(task.exception?.message))
                    }
                }
            }
    }

    override fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }

    override fun isEmailVerified() = auth.currentUser?.isEmailVerified == true
}

object MockFirebaseAuthRepositoryImpl : FirebaseAuthRepository {
    override fun createUserWithEmailAndPassword(email: String, password: String, onSuccess: (FirebaseUser?) -> Unit, onFailure: (RegisterFailure) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun storeAccountDataToFirestore(user: User, onSuccess: () -> Unit, onFailure: (String?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun signInWithEmailAndPassword(email: String, password: String, onSuccess: () -> Unit, onFailure: (LoginFailure) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun sendEmailVerification() {
        TODO("Not yet implemented")
    }
    
    override fun isEmailVerified(): Boolean {
        TODO("Not yet implemented")
    }
}