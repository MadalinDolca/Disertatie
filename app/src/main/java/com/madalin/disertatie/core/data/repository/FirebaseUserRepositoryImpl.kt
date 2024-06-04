package com.madalin.disertatie.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.madalin.disertatie.core.data.CollectionPath
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.domain.result.UserResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirebaseUserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FirebaseUserRepository {

    override fun getCurrentUserId() = auth.currentUser?.uid

    override fun isSignedIn(): Boolean {
        auth.currentUser?.getIdToken(true) // refresh token
        return auth.currentUser != null
    }

    override suspend fun observerUserData() = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(UserResult.NoUserId)
            close()
        } else {
            val docRef = firestore.collection(CollectionPath.USERS).document(userId)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UserResult.DataFetchingError)
                    close()
                }
                if (snapshot != null && snapshot.exists()) {
                    val userData = snapshot.toObject<User>()

                    if (userData == null) {
                        trySend(UserResult.UserDataNotFound)
                        close()
                    } else {
                        userData.id = snapshot.id
                        trySend(UserResult.Success(userData))
                    }
                } else {
                    trySend(UserResult.UserDataNotFound)
                    close()
                }
            }
            awaitClose { listener.remove() }
        }
    }

    override fun signOut(onSuccess: () -> Unit, onFailure: (String?) -> Unit) {
        try {
            auth.signOut()
            onSuccess()
        } catch (e: Exception) {
            onFailure(e.message)
        }
    }
}