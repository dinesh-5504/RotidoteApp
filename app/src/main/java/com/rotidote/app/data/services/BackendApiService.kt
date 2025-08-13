package com.rotidote.app.data.services

import okhttp3.MultipartBody
import retrofit2.http.*

interface BackendApiService {
    @POST("create-upload")
    suspend fun createUploadUrl(@Body request: CreateUploadRequest): CreateUploadResponse

    @GET("asset/{assetId}")
    suspend fun getAssetDetails(@Path("assetId") assetId: String): AssetDetailsResponse

    @GET("health")
    suspend fun healthCheck(): HealthResponse

    @POST("upload-video")
    suspend fun uploadVideo(
        @Part("creatorName") creatorName: String,
        @Part("videoTitle") videoTitle: String,
        @Part("duration") duration: String,
        @Part adVideo: MultipartBody.Part,
        @Part mainVideo: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): VideoUploadResponse
}

data class CreateUploadRequest(
    val filename: String,
    val contentType: String
)

data class CreateUploadResponse(
    val uploadUrl: String,
    val uploadId: String,
    val assetId: String
)

data class AssetDetailsResponse(
    val assetId: String,
    val playbackId: String?,
    val status: String,
    val duration: Double?,
    val aspectRatio: String?,
    val createdAt: String?
)

data class HealthResponse(
    val status: String,
    val timestamp: String,
    val services: ServicesStatus
)

data class ServicesStatus(
    val mux: Boolean,
    val cloudinary: Boolean,
    val firebase: Boolean
)

data class VideoUploadResponse(
    val success: Boolean,
    val videoId: String,
    val message: String,
    val data: VideoUploadData
)

data class VideoUploadData(
    val adVideoAssetId: String,
    val mainVideoAssetId: String,
    val thumbnailUrl: String,
    val videoData: VideoData
)

data class VideoData(
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val duration: Double,
    val adVideoMuxKey: String,
    val mainVideoMuxKey: String,
    val adVideoPlaybackId: String?,
    val mainVideoPlaybackId: String?,
    val thumbnailUrl: String,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?
)
