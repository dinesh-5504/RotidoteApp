package com.rotidote.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.Video
import com.rotidote.app.data.services.FirestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val firestoreService: FirestoreService
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
    
    // New state flows for player controls
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // Videos for related content
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()
    
    private var skipAd = false
    
    init {
        // Load videos for related content
        loadVideos()
    }
    
    private fun loadVideos() {
        viewModelScope.launch {
            try {
                val videosList = firestoreService.getVideos(20)
                _videos.value = videosList
            } catch (e: Exception) {
                // Handle error silently for related videos
            }
        }
    }
    
    fun loadVideo(videoId: String, skipAd: Boolean = false) {
        this.skipAd = skipAd
        Log.d("VideoPlayerViewModel", "Loading video with ID: $videoId, skipAd: $skipAd")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val video = firestoreService.getVideo(videoId)
                if (video != null) {
                    Log.d("VideoPlayerViewModel", "Video found: ${video.title}")
                    _video.value = video
                    loadVideoUrls(video)
                    
                    // If skipAd is true, skip directly to main video
                    if (skipAd) {
                        _isAdPlaying.value = false
                    }
                } else {
                    Log.e("VideoPlayerViewModel", "Video not found for ID: $videoId")
                    _error.value = "Video not found"
                }
            } catch (e: Exception) {
                Log.e("VideoPlayerViewModel", "Error loading video: ${e.message}", e)
                _error.value = e.message ?: "Failed to load video"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadVideoUrls(video: Video) {
        try {
            // Validate playback IDs
            if (video.mainVideo.playbackId.isEmpty()) {
                Log.e("VideoPlayerViewModel", "Main video playbackId is empty")
                _error.value = "Main video playback ID is missing"
                return
            }
            
            if (video.adVideo.playbackId.isEmpty()) {
                Log.w("VideoPlayerViewModel", "Ad video playbackId is empty, will skip ad")
                _adVideoUrl.value = null
            } else {
                val adUrl = "https://stream.mux.com/${video.adVideo.playbackId}.m3u8"
                Log.d("VideoPlayerViewModel", "Ad video URL: $adUrl")
                _adVideoUrl.value = adUrl
            }
            
            val mainUrl = "https://stream.mux.com/${video.mainVideo.playbackId}.m3u8"
            Log.d("VideoPlayerViewModel", "Main video URL: $mainUrl")
            _mainVideoUrl.value = mainUrl
            
        } catch (e: Exception) {
            Log.e("VideoPlayerViewModel", "Error loading video URLs: ${e.message}", e)
            _error.value = e.message ?: "Failed to load video URLs"
        }
    }
    
    // Player control functions
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }
    
    fun seekTo(position: Long) {
        _currentPosition.value = position.coerceIn(0, _duration.value)
    }
    
    fun skipForward() {
        val newPosition = _currentPosition.value + 5000 // 5 seconds
        _currentPosition.value = newPosition.coerceIn(0, _duration.value)
    }
    
    fun skipBackward() {
        val newPosition = _currentPosition.value - 5000 // 5 seconds
        _currentPosition.value = newPosition.coerceIn(0, _duration.value)
    }
    
    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }
    
    fun updateDuration(duration: Long) {
        _duration.value = duration
    }
    
    fun onAdFinished() {
        _isAdPlaying.value = false
    }
    
    fun onMainVideoFinished() {
        // Handle main video completion
    }
    
    fun clearError() {
        _error.value = null
    }
} 