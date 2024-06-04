package com.madalin.disertatie.core.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Object that contains the names of the collections from [Firestore][Firebase.firestore].
 */
object CollectionPath {
    const val USERS = "users"
    const val TRAILS = "trails"
    const val TRAIL_POINTS_LIST = "trailPointsList"
    const val IMAGES_LIST = "imagesList"
}

object StoragePath {
    const val TRAIL_IMAGES = "trailImages"
}