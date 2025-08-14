package com.rotidote.app.data.services

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuxService @Inject constructor(
    private val backendApiService: BackendApiService,
    private val okHttpClient: OkHttpClient
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

    suspend fun uploadVideoToMux(videoUri: Uri, context: Context): Result<MuxUploadResult> {
        return try {
            withContext(Dispatchers.IO) {
                // Create upload URL from backend
                val uploadResponse = createUploadUrl(
                    filename = "video_${System.currentTimeMillis()}.mp4",
                    contentType = "video/mp4"
                ).getOrThrow()

                // Upload video directly to Mux using the upload URL
                val videoFile = File(videoUri.path!!)
                val requestBody = videoFile.asRequestBody("video/mp4".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData("file", videoFile.name, requestBody)

                // Create request to upload to Mux
                val request = okhttp3.Request.Builder()
                    .url(uploadResponse.uploadUrl)
                    .post(requestBody)
                    .build()

                // Execute upload
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    // Wait a bit for Mux to process the video
                    kotlinx.coroutines.delay(2000)
                    
                    // Get asset details
                    val assetDetails = backendApiService.getAssetDetails(uploadResponse.assetId)
                    
                    val result = MuxUploadResult(
                        assetId = uploadResponse.assetId,
                        playbackId = assetDetails.playbackId,
                        playbackUrl = "https://stream.mux.com/${assetDetails.playbackId}.m3u8",
                        status = assetDetails.status
                    )
                    Result.success(result)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoPlaybackUrl(assetId: String): Result<String> {
        return try {
            val assetDetails = backendApiService.getAssetDetails(assetId)
            val playbackUrl = "https://stream.mux.com/${assetDetails.playbackId}.m3u8"
            Result.success(playbackUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadThumbnailToCloudinary(imageUri: Uri, context: Context): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                // Read the image file
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image file")
                inputStream?.close()

                // Create multipart body
                val requestBody = okhttp3.RequestBody.create(
                    "image/*".toMediaType(),
                    bytes
                )
                val multipartBody = MultipartBody.Part.createFormData(
                    "thumbnail",
                    "thumbnail_${System.currentTimeMillis()}.jpg",
                    requestBody
                )

                // Upload to backend Cloudinary endpoint
                val response = backendApiService.uploadThumbnail(multipartBody)
                Result.success(response.thumbnailUrl)
            }
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
                aspectRatio = assetDetails.aspectRatio ?: "16:9"
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

data class MuxUploadResult(
    val assetId: String,
    val playbackId: String,
    val playbackUrl: String,
    val status: String
)

data class VideoMetadata(
    val duration: Long,
    val width: Int,
    val height: Int,
    val aspectRatio: String
) 