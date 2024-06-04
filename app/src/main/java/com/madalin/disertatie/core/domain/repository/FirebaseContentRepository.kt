package com.madalin.disertatie.core.domain.repository

import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.result.TrailDeleteResult
import com.madalin.disertatie.core.domain.result.TrailImagesResult
import com.madalin.disertatie.core.domain.result.TrailInfoResult
import com.madalin.disertatie.core.domain.result.TrailPointsResult
import com.madalin.disertatie.core.domain.result.TrailUpdateResult
import com.madalin.disertatie.core.domain.result.TrailsListResult
import kotlinx.coroutines.flow.Flow

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
     * Retrieves the trails belonging to the user with the given [userId] as a [TrailsListResult]
     * and listens for changes in the database.
     */
    suspend fun observeTrailsByUserId(userId: String): Flow<TrailsListResult>

    /**
     * Retrieves the trail with the given [trailId] as a [TrailInfoResult].
     */
    suspend fun getTrailInfoById(trailId: String): TrailInfoResult

    /**
     * Retrieves the trail points belonging to the trail with the given [trailId] as a [TrailPointsResult].
     */
    suspend fun getTrailPointsByTrailId(trailId: String): TrailPointsResult

    /**
     * Retrieves the trail images belonging to the trail with the given [trailId] as a [TrailImagesResult].
     */
    suspend fun getTrailImagesByTrailId(trailId: String): TrailImagesResult

    /**
     * Updates the trail with the given [trailId] with the given [newData].
     */
    suspend fun updateTrailById(trailId: String, newData: Map<String, Any>): TrailUpdateResult

    /**
     * Deletes the trail with the given [trailId].
     */
    suspend fun deleteTrailById(trailId: String): TrailDeleteResult
}