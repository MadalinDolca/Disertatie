package com.madalin.disertatie.core.data

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.madalin.disertatie.core.data.util.imageUploadTasks
import com.madalin.disertatie.core.data.util.trailWriteTasks
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.result.TrailInfoError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FirebaseContentRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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

    override fun getTrailsByUserId(
        userId: String,
        onSuccess: (trails: List<Trail>) -> Unit, onFailure: (message: String?) -> Unit
    ) {
        val trailsList = mutableListOf<Trail>()

        firestore.collection(CollectionPath.TRAILS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error.message)
                    return@addSnapshotListener
                }

                trailsList.clear()

                snapshots?.forEach {
                    trailsList.add(it.toObject<Trail>())
                }

                onSuccess(trailsList)
            }
    }

    override fun getTrailInfoById(
        trailId: String,
        onSuccess: (trail: Trail) -> Unit, onFailure: (result: TrailInfoError) -> Unit
    ) {
        firestore.collection(CollectionPath.TRAILS).document(trailId)
            .get()
            .addOnSuccessListener { snapshot ->
                val trail = snapshot.toObject<Trail>()

                if (trail != null) {
                    onSuccess(trail)
                } else {
                    onFailure(TrailInfoError.NotFound)
                }
            }
            .addOnFailureListener {
                onFailure(TrailInfoError.Error(it.message))
            }
    }

    override fun getTrailPointsByTrailId(
        trailId: String,
        onSuccess: (trailPoints: MutableList<TrailPoint>) -> Unit, onFailure: (message: String?) -> Unit
    ) {
        firestore.collection(CollectionPath.TRAILS).document(trailId)
            .collection(CollectionPath.TRAIL_POINTS_LIST)
            .get()
            .addOnSuccessListener { snapshots ->
                val trailPoints = mutableListOf<TrailPoint>()

                snapshots.forEach {
                    trailPoints.add(it.toObject<TrailPoint>())
                }
                onSuccess(trailPoints)
            }
            .addOnFailureListener {
                onFailure(it.message)
            }
    }

    override fun getTrailImagesByTrailId(
        trailId: String,
        onSuccess: (images: List<String>) -> Unit, onFailure: (message: String?) -> Unit
    ) {
        val storageRef = storage.getReference(StoragePath.TRAIL_IMAGES).child(trailId)

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val imagesList = mutableListOf<String>()

                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        imagesList.add(uri.toString())
                    }
                }

                onSuccess(imagesList)
            }
            .addOnFailureListener {
                onFailure(it.message)
            }
    }

    override fun updateTrailById(
        trailId: String, newData: Map<String, Any>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    ) {
        firestore.collection(CollectionPath.TRAILS).document(trailId)
            .update(newData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }

    override fun deleteTrailById(
        trailId: String,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    ) {
        // TODO delete trailImages collection from document and delete images from storage
        firestore.collection(CollectionPath.TRAILS).document(trailId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }
}