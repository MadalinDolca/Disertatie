package com.madalin.disertatie.core.data

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.madalin.disertatie.core.data.util.imageUploadTasks
import com.madalin.disertatie.core.data.util.trailWriteTasks
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository

class FirebaseContentRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : FirebaseContentRepository {

    override fun saveTrail(
        trail: Trail,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    ) {
        val trailWriteTasks = trailWriteTasks(firestore, trail)
        val imageUploadTasks = imageUploadTasks(storage, trail)
        val allTasks = mutableListOf<Task<*>>()

        allTasks.add(trailWriteTasks)
        allTasks.addAll(imageUploadTasks)

        Tasks.whenAll(allTasks)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message)
            }
    }

    override fun getUserTrails(
        userId: String,
        onSuccess: (trails: List<Trail>) -> Unit, onFailure: (message: String?) -> Unit
    ) {

    }
}