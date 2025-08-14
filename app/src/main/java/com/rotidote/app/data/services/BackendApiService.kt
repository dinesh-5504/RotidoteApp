package com.rotidote.app.data.services

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import okhttp3.MultipartBody
import retrofit2.http.Part

interface BackendApiService {
    @POST("create-upload")
    suspend fun createUploadUrl(@Body request: CreateUploadRequest): CreateUploadResponse

    @GET("asset/{assetId}")
    suspend fun getAssetDetails(@Path("assetId") assetId: String): AssetDetailsResponse

    @POST("upload-thumbnail")
    suspend fun uploadThumbnail(@Part thumbnail: MultipartBody.Part): ThumbnailUploadResponse

    @GET("health")
    suspend fun healthCheck(): HealthResponse
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
    val playbackId: String,
    val status: String,
    val duration: Double,
    val aspectRatio: String,
    val createdAt: String
)

data class ThumbnailUploadResponse(
    val thumbnailUrl: String,
    val publicId: String,
    val width: Int,
    val height: Int
)

data class HealthResponse(
    val status: String,
    val timestamp: String
)
