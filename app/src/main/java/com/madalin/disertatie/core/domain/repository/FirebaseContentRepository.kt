package com.madalin.disertatie.core.domain.repository

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.domain.result.TrailInfoError

interface FirebaseContentRepository {
    /**
     * Writes this [trail] into the database and stores its images on cloud storage.
     * The trail points will be written to a collection inside the document of this trail.
     * @param onSuccess Invoked when the operation succeeds.
     * @param onFailure Invoked when the operation fails.
     */
    fun saveTrail(
        trail: Trail,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )

    /**
     * Retrieves the trails belonging to this [userId].
     * @param onSuccess Invoked when the operation succeeds with the trails list as parameter.
     * @param onFailure Invoked when the operation fails.
     */
    fun getTrailsByUserId(
        userId: String,
        onSuccess: (trails: List<Trail>) -> Unit, onFailure: (message: String?) -> Unit
    )

    fun getTrailInfoById(
        trailId: String,
        onSuccess: (trail: Trail) -> Unit, onFailure: (error: TrailInfoError) -> Unit
    )

    fun getTrailPointsByTrailId(
        trailId: String,
        onSuccess: (trailPoints: MutableList<TrailPoint>) -> Unit, onFailure: (message: String?) -> Unit
    )

    fun getTrailImagesByTrailId(
        trailId: String,
        onSuccess: (images: List<String>) -> Unit, onFailure: (message: String?) -> Unit
    )

    fun updateTrailById(
        trailId: String, newData: Map<String, Any>,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )

    fun deleteTrailById(
        trailId: String,
        onSuccess: () -> Unit, onFailure: (message: String?) -> Unit
    )
}