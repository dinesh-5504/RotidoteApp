package com.rotidote.app.data.services

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {
    @POST("create-upload")
    suspend fun createUploadUrl(@Body request: CreateUploadRequest): CreateUploadResponse

    @GET("asset/{assetId}")
    suspend fun getAssetDetails(@Path("assetId") assetId: String): AssetDetailsResponse

    @GET("health")
    suspend fun healthCheck(): HealthResponse
}

data class CreateUploadRequest(
    val filename: String,
    val contentType: String
)

data class CreateUploadResponse(
    val uploadUrl: String,
    val assetId: String
)

data class AssetDetailsResponse(
    val id: String,
    val status: String,
    val playbackIds: List<PlaybackId>,
    val duration: Double
)

data class PlaybackId(
    val id: String,
    val policy: String
)

data class HealthResponse(
    val status: String,
    val timestamp: String
)
