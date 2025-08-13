package com.rotidote.app.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.Video
import com.rotidote.app.data.services.FirestoreService
import com.rotidote.app.data.services.MuxService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val muxService: MuxService
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun uploadVideo(
        context: Context,
        creatorName: String,
        videoTitle: String,
        duration: Long,
        adVideoUri: Uri,
        mainVideoUri: Uri,
        thumbnailUri: Uri
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0f
            _error.value = null
            _uploadSuccess.value = false

            try {
                // Upload ad video to Mux
                _uploadProgress.value = 0.1f
                val adVideoResult = muxService.uploadVideoToMux(adVideoUri, context)
                val adVideoAssetId = adVideoResult.getOrThrow()

                // Upload main video to Mux
                _uploadProgress.value = 0.3f
                val mainVideoResult = muxService.uploadVideoToMux(mainVideoUri, context)
                val mainVideoAssetId = mainVideoResult.getOrThrow()

                // Upload thumbnail to Cloudinary
                _uploadProgress.value = 0.5f
                val thumbnailResult = muxService.uploadThumbnailToCloudinary(thumbnailUri, context)
                val thumbnailUrl = thumbnailResult.getOrThrow()

                // Get video metadata
                _uploadProgress.value = 0.7f
                val metadataResult = muxService.getVideoMetadata(mainVideoAssetId)
                val metadata = metadataResult.getOrThrow()

                // Create video object
                val video = Video(
                    title = videoTitle,
                    creatorName = creatorName,
                    duration = metadata.duration,
                    adVideoMuxKey = adVideoAssetId,
                    mainVideoMuxKey = mainVideoAssetId,
                    thumbnailUrl = thumbnailUrl
                )

                _uploadProgress.value = 0.9f

                // Save to Firestore
                val saveResult = firestoreService.saveVideo(video)
                saveResult.fold(
                    onSuccess = {
                        _uploadProgress.value = 1f
                        _uploadSuccess.value = true
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Failed to save video"
                    }
                )

            } catch (e: Exception) {
                _error.value = e.message ?: "Upload failed"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun resetUploadState() {
        _uploadSuccess.value = false
        _uploadProgress.value = 0f
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
} 