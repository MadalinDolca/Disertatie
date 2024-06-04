package com.madalin.disertatie.core.data.util

import com.madalin.disertatie.core.domain.model.TrailPoint

/**
 * Maps TrailPoint images to their corresponding image URLs.
 */
fun mapTrailPointsAndImageUrls(trailPoints: List<TrailPoint>, imageUrls: List<String>) {
    for (trailPoint in trailPoints) {
        for (trailImage in trailPoint.imagesList) {
            for (imageUrl in imageUrls) {
                val extractedId = extractImageIdFromUrl(imageUrl)

                if (trailImage.id == extractedId) {
                    trailImage.imageUrl = imageUrl
                    break // stop searching after finding a match
                }
            }
        }
    }
}

/**
 * Extracts a TrailImage ID from the given [imageUrl] and returns it.
 */
private fun extractImageIdFromUrl(imageUrl: String): String? {
    val regex = Regex("%2F([a-f0-9]{32})\\?") // regex pattern that captures everything between %2F and ?
    val match = regex.find(imageUrl)

    return match?.groupValues?.get(1)
}