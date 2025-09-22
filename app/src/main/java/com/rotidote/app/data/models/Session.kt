
package com.rotidote.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Session(
    val dayId: String = "",
    val title: String = "",
    val enabled: Boolean = false,
    val videos: List<String> = emptyList(), // List of video IDs
    val permittedStudents: List<String> = emptyList(), // List of user IDs
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class AdminVideo(
    val videoId: String = "",
    val title: String = "",
    val description: String = "",
    val duration: Int = 0, // in seconds
    val muxPlaybackId: String = "",
    val assignedDay: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

data class AdminCredentials(
    val email: String = "dineshkarthikeyan.admin@gmail.com",
    val password: String = "RotidoteApp"
)

