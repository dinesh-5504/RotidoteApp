package com.rotidote.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val videoId: String = "", // Generated ID for the document
    val title: String = "",
    val creatorName: String = "",
    val duration: String = "", // Format: "05:32"
    val mainGenre: String = "",
    val mainVideo: MainVideo = MainVideo(),
    val adGenre: String = "",
    val adVideo: AdVideo = AdVideo(),
    val orientation: String = "landscape" // "portrait" or "landscape"
) : Parcelable

@Parcelize
data class MainVideo(
    val playbackId: String = "",
    val thumbnailUrl: String = "" // Cloudinary URL
) : Parcelable

@Parcelize
data class AdVideo(
    val playbackId: String = ""
) : Parcelable 