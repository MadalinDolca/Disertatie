package com.madalin.disertatie.core.data.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.madalin.disertatie.core.data.CollectionPath
import com.madalin.disertatie.core.data.StoragePath
import com.madalin.disertatie.core.domain.extension.toByteArray
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailImage

/**
 * Sets the storage path for the [trail] images and creates an [UploadTask] for each of them.
 * @return The list of image upload tasks.
 */
fun imageUploadTasks(
    storage: FirebaseStorage,
    trail: Trail
): List<UploadTask> {
    val storageRef = storage.getReference(StoragePath.TRAIL_IMAGES).child(trail.id)
    val uploadTasks = mutableListOf<UploadTask>()

    trail.extractTrailImages().forEach {
        val uploadTask = storageRef.child(it.id).putBytes(it.image.toByteArray())
        uploadTasks.add(uploadTask)
    }

    return uploadTasks
}

/**
 * Sets document references for this [trail] and its [trail images][TrailImage] to create a batch
 * [Task] out of them. The image documents will be places in a collection inside the trail document.
 * @return The write batch task as a single atomic unit.
 */
fun trailWriteTasks(
    firestore: FirebaseFirestore,
    trail: Trail
): Task<Void> {
    val batch = firestore.batch()

    // writes the trail (without the points) into a document with the same ID
    val trailRef = firestore.collection(CollectionPath.TRAILS).document(trail.id)
    batch.set(trailRef, trail)

    // inside the trail document, it writes the trail's points
    trail.trailPointsList.forEach { trailPoint ->
        val trailPointRef = trailRef.collection(CollectionPath.TRAIL_POINTS_LIST).document(trailPoint.id)
        batch.set(trailPointRef, trailPoint)
    }

    // commits the writes as a single atomic unit
    return batch.commit()
}