package com.madalin.disertatie.core.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.madalin.disertatie.core.domain.extension.toByteArray
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailImage
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
        val batch = firestore.batch()

        // writes the trail (without the points) into a document with the same ID
        val trailRef = firestore.collection(DBCollection.TRAILS).document(trail.id)
        batch.set(trailRef, trail)

        // inside the trail document, it writes the trail's points
        trail.trailPointsList.forEach { trailPoint ->
            val trailPointRef = trailRef.collection(DBCollection.TRAIL_POINTS_LIST).document(trailPoint.id)
            batch.set(trailPointRef, trailPoint)
        }

        // commits the writes as a single atomic unit
        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure(task.exception?.message)
            }
        }
    }

    override fun storeTrailImages(
        trailId: String, images: List<TrailImage>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    ) {
        val storageRef = storage.getReference(StoragePath.TRAIL_IMAGES).child(trailId)
        val uploadTasks = mutableListOf<UploadTask>()

        images.forEach {
            val uploadTask = storageRef.child(it.id).putBytes(it.image.toByteArray())
            uploadTasks.add(uploadTask)
        }

        Tasks.whenAll(uploadTasks)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message)
            }
    }

    override fun saveTrailAndStoreImages(
        trail: Trail, images: List<TrailImage>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    ) {
        saveTrail(trail,
            onSuccess = { storeTrailImages(trail.id, images, onSuccess, onFailure) },
            onFailure = { onFailure(it) }
        )
    }
}