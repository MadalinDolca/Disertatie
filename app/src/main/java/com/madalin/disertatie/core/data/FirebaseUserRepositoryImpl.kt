package com.madalin.disertatie.core.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.disertatie.core.domain.model.User
import com.madalin.disertatie.core.domain.result.UserFailure
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository

class FirebaseUserRepositoryImpl : FirebaseUserRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override fun getCurrentUserId() = auth.currentUser?.uid

    override fun isSignedIn(): Boolean {
        auth.currentUser?.getIdToken(true) // refresh token
        return auth.currentUser != null
    }

    override fun startListeningForUserData(onSuccess: (User) -> Unit, onFailure: (UserFailure) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(UserFailure.NoUserId)
            return
        }

        firestore.collection(DBCollection.USERS).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(UserFailure.DataFetchingError)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userData = snapshot.toObject<User>()

                    if (userData == null) {
                        onFailure(UserFailure.UserDataNotFound)
                        return@addSnapshotListener
                    }

                    userData.id = snapshot.id
                    onSuccess(userData)
                } else {
                    onFailure(UserFailure.UserDataNotFound)
                }
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

object MockFirebaseUserRepositoryImpl : FirebaseUserRepository {
    override fun isSignedIn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentUserId(): String? {
        TODO("Not yet implemented")
    }

    override fun startListeningForUserData(onSuccess: (User) -> Unit, onFailure: (UserFailure) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun signOut(onSuccess: () -> Unit, onFailure: (String?) -> Unit) {
        TODO("Not yet implemented")
    }
}