package com.rotidote.app.data.services

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryService @Inject constructor() {

    fun initialize(context: Context, cloudName: String) {
        val config = HashMap<String, String>()
        config["cloud_name"] = cloudName
        MediaManager.init(context, config)
    }

    suspend fun uploadImage(imageUri: Uri, publicId: String? = null): Result<String> {
        return try {
            val result = suspendCancellableCoroutine<String> { continuation ->
                val requestId = MediaManager.get()
                    .upload(imageUri)
                    .option("public_id", publicId ?: "rotidote_${System.currentTimeMillis()}")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            // Upload started
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            // Upload progress
                        }

                        override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                            val secureUrl = resultData?.get("secure_url") as? String
                            if (secureUrl != null) {
                                continuation.resume(secureUrl)
                            } else {
                                continuation.resumeWithException(Exception("Upload failed: No secure URL returned"))
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Upload failed: ${error?.description}"))
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Upload rescheduled: ${error?.description}"))
                        }
                    })
                    .dispatch()
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

