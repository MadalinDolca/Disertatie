package com.madalin.disertatie.core.domain.entity

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var role: String = ROLE_DEFAULT,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_DEFAULT = "default"
    }
}