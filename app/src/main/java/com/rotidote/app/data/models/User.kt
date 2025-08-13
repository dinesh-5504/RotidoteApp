package com.rotidote.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val grade: String = "",
    val section: String = "",
    val schoolName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable 