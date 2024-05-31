package com.madalin.disertatie.core.domain.repository

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailImage

interface FirebaseContentRepository {
    /**
     * Saves the given [trail] to the database. The [trail images][TrailImage] of this trail will
     * be saved to a different collection inside the document of this trail.
     * @param onSuccess Invoked when the operation succeeds.
     * @param onFailure Invoked when the operation fails.
     */
    fun saveTrail(
        trail: Trail,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )

    /**
     * Stores the given [images] of the trail at this [trailId] on cloud storage.
     * @param onSuccess Invoked when the operation succeeds.
     * @param onFailure Invoked when the operation fails.
     */
    fun storeTrailImages(
        trailId: String, images: List<TrailImage>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )

    /**
     * Saves this [trail] into the database and stores these [images] in the cloud.
     * @param onSuccess Invoked when the operation succeeds.
     * @param onFailure Invoked when the operation fails.
     */
    fun saveTrailAndStoreImages(
        trail: Trail, images: List<TrailImage>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )

    /**
     * Retrieves the trails belonging to this [userId].
     * @param onSuccess Invoked when the operation succeeds with the trails list as parameter.
     * @param onFailure Invoked when the operation fails.
     */
    fun getUserTrails(
        userId: String,
        onSuccess: (trails: List<Trail>) -> Unit, onFailure: (message: String?) -> Unit
    )
}