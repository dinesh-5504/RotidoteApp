package com.rotidote.app.data.services

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuxService @Inject constructor(
    private val backendApiService: BackendApiService,
    private val cloudinaryService: CloudinaryService
) {

    suspend fun createUploadUrl(filename: String, contentType: String): Result<CreateUploadResponse> {
        return try {
            val request = CreateUploadRequest(filename, contentType)
            val response = backendApiService.createUploadUrl(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadVideoToMux(videoUri: Uri, context: Context): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                // Create upload URL
                val uploadResponse = createUploadUrl(
                    filename = "video_${System.currentTimeMillis()}.mp4",
                    contentType = "video/mp4"
                ).getOrThrow()

                // Upload video to Mux
                val videoFile = File(videoUri.path!!)
                val requestBody = videoFile.asRequestBody("video/mp4".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData("file", videoFile.name, requestBody)

                // For now, we'll return the asset ID
                // In a real implementation, you would upload to the Mux URL
                Result.success(uploadResponse.assetId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoPlaybackUrl(assetId: String): Result<String> {
        return try {
            val assetDetails = backendApiService.getAssetDetails(assetId)
            val playbackId = assetDetails.playbackIds.firstOrNull()?.id
                ?: throw Exception("No playback ID available for asset")
            
            val playbackUrl = "https://stream.mux.com/$playbackId.m3u8"
            Result.success(playbackUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadThumbnailToCloudinary(imageUri: Uri, context: Context): Result<String> {
        return try {
            cloudinaryService.uploadImage(imageUri, "rotidote_thumbnail_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoMetadata(assetId: String): Result<VideoMetadata> {
        return try {
            val assetDetails = backendApiService.getAssetDetails(assetId)
            val metadata = VideoMetadata(
                duration = ((assetDetails.duration ?: 0.0) * 1000).toLong(), // Convert to milliseconds
                width = 1920, // Default values since Mux doesn't provide these in basic response
                height = 1080,
                aspectRatio = "16:9" // Default aspect ratio
            )
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun healthCheck(): Result<Boolean> {
        return try {
            val response = backendApiService.healthCheck()
            Result.success(response.status == "OK")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class VideoMetadata(
    val duration: Long,
    val width: Int,
    val height: Int,
    val aspectRatio: String
) 