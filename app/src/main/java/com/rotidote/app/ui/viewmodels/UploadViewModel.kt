package com.rotidote.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.Video
import com.rotidote.app.data.models.MainVideo
import com.rotidote.app.data.models.AdVideo
import com.rotidote.app.data.services.FirestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun uploadVideo(
        creatorName: String,
        videoTitle: String,
        duration: String,
        mainGenre: String,
        mainVideoPlaybackId: String,
        adGenre: String,
        adVideoPlaybackId: String,
        thumbnailUrl: String,
        orientation: String
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            _uploadSuccess.value = false

            try {
                // Create video object with the new structure
                val video = Video(
                    title = videoTitle,
                    creatorName = creatorName,
                    duration = duration,
                    mainGenre = mainGenre,
                    mainVideo = MainVideo(
                        playbackId = mainVideoPlaybackId,
                        thumbnailUrl = thumbnailUrl
                    ),
                    adGenre = adGenre,
                    adVideo = AdVideo(
                        playbackId = adVideoPlaybackId
                    ),
                    orientation = orientation
                )

                // Save to Firestore
                val saveResult = firestoreService.saveVideo(video)
                saveResult.fold(
                    onSuccess = {
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
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
} 