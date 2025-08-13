package com.rotidote.app.ui.viewmodels

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
class VideoPlayerViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val muxService: MuxService
) : ViewModel() {
    
    private val _video = MutableStateFlow<Video?>(null)
    val video: StateFlow<Video?> = _video.asStateFlow()
    
    private val _adVideoUrl = MutableStateFlow<String?>(null)
    val adVideoUrl: StateFlow<String?> = _adVideoUrl.asStateFlow()
    
    private val _mainVideoUrl = MutableStateFlow<String?>(null)
    val mainVideoUrl: StateFlow<String?> = _mainVideoUrl.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isAdPlaying = MutableStateFlow(true)
    val isAdPlaying: StateFlow<Boolean> = _isAdPlaying.asStateFlow()
    
    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val video = firestoreService.getVideo(videoId)
                if (video != null) {
                    _video.value = video
                    loadVideoUrls(video)
                } else {
                    _error.value = "Video not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load video"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadVideoUrls(video: Video) {
        try {
            // Load ad video URL
            val adUrlResult = muxService.getVideoPlaybackUrl(video.adVideoMuxKey)
            adUrlResult.fold(
                onSuccess = { url -> _adVideoUrl.value = url },
                onFailure = { exception -> _error.value = "Failed to load ad video: ${exception.message}" }
            )
            
            // Load main video URL
            val mainUrlResult = muxService.getVideoPlaybackUrl(video.mainVideoMuxKey)
            mainUrlResult.fold(
                onSuccess = { url -> _mainVideoUrl.value = url },
                onFailure = { exception -> _error.value = "Failed to load main video: ${exception.message}" }
            )
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load video URLs"
        }
    }
    
    fun onAdFinished() {
        _isAdPlaying.value = false
    }
    
    fun onMainVideoFinished() {
        // Handle main video completion
    }
    
    fun likeVideo() {
        viewModelScope.launch {
            val currentVideo = _video.value
            if (currentVideo != null) {
                val newLikes = currentVideo.likes + 1
                firestoreService.updateVideoLikes(currentVideo.id, newLikes)
                _video.value = currentVideo.copy(likes = newLikes)
            }
        }
    }
    
    fun dislikeVideo() {
        viewModelScope.launch {
            val currentVideo = _video.value
            if (currentVideo != null) {
                val newDislikes = currentVideo.dislikes + 1
                firestoreService.updateVideoDislikes(currentVideo.id, newDislikes)
                _video.value = currentVideo.copy(dislikes = newDislikes)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 