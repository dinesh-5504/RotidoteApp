package com.rotidote.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: String = "",
    val title: String = "",
    val creatorName: String = "",
    val creatorId: String = "",
    val duration: Long = 0L,
    val adVideoMuxKey: String = "",
    val mainVideoMuxKey: String = "",
    val adVideoPlaybackUrl: String = "",
    val mainVideoPlaybackUrl: String = "",
    val thumbnailUrl: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0,
    val comments: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable 