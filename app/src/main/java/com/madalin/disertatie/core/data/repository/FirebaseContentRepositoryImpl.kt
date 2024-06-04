package com.madalin.disertatie.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.madalin.disertatie.core.data.CollectionPath
import com.madalin.disertatie.core.data.StoragePath
import com.madalin.disertatie.core.data.util.imageUploadTasks
import com.madalin.disertatie.core.data.util.trailWriteTasks
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.result.TrailDeleteResult
import com.madalin.disertatie.core.domain.result.TrailImagesResult
import com.madalin.disertatie.core.domain.result.TrailInfoResult
import com.madalin.disertatie.core.domain.result.TrailPointsResult
import com.madalin.disertatie.core.domain.result.TrailUpdateResult
import com.madalin.disertatie.core.domain.result.TrailsListResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

    override suspend fun observeTrailsByUserId(userId: String) = callbackFlow {
        val colRef = firestore.collection(CollectionPath.TRAILS)
        val query = colRef.whereEqualTo("userId", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(TrailsListResult.Error(error.message))
                close()
            }
            if (snapshot != null) {
                val trails = snapshot.toObjects<Trail>()
                trySend(TrailsListResult.Success(trails))
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getTrailInfoById(trailId: String): TrailInfoResult {
        val docRef = firestore
            .collection(CollectionPath.TRAILS)
            .document(trailId)

        try {
            val trail = docRef.get().await().toObject<Trail>()

            return if (trail != null) TrailInfoResult.Success(trail)
            else TrailInfoResult.NotFound
        } catch (e: Exception) {
            return TrailInfoResult.Error(e.message)
        }
    }

    override suspend fun getTrailPointsByTrailId(trailId: String): TrailPointsResult {
        val colRef = firestore
            .collection(CollectionPath.TRAILS)
            .document(trailId)
            .collection(CollectionPath.TRAIL_POINTS_LIST)

        try {
            val trailPoints = colRef.get().await().toObjects<TrailPoint>()
            return TrailPointsResult.Success(trailPoints)
        } catch (e: Exception) {
            return TrailPointsResult.Error(e.message)
        }
    }

    override suspend fun getTrailImagesByTrailId(trailId: String): TrailImagesResult {
        val ref = storage.getReference(StoragePath.TRAIL_IMAGES).child(trailId)

        try {
            val images = mutableListOf<String>()
            ref.listAll().await().items.forEach {
                images.add(it.downloadUrl.await().toString())
            }
            return TrailImagesResult.Success(images)
        } catch (e: Exception) {
            return TrailImagesResult.Error(e.message)
        }
    }

    override suspend fun updateTrailById(
        trailId: String, newData: Map<String, Any>
    ): TrailUpdateResult {
        val docRef = firestore.collection(CollectionPath.TRAILS).document(trailId)

        try {
            docRef.update(newData).await()
            return TrailUpdateResult.Success
        } catch (e: Exception) {
            return TrailUpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteTrailById(trailId: String): TrailDeleteResult {
        // TODO delete trailImages collection from document and delete images from storage
        val doc = firestore.collection(CollectionPath.TRAILS).document(trailId)

        try {
            doc.delete().await()
            return TrailDeleteResult.Success
        } catch (e: Exception) {
            return TrailDeleteResult.Error(e.message)
        }
    }
}