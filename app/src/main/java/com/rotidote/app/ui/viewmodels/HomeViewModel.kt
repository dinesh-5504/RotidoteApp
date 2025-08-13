package com.rotidote.app.ui.viewmodels

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
class HomeViewModel @Inject constructor(
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("For You")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadVideos()
    }
    
    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val videoList = firestoreService.getVideos(50)
                _videos.value = videoList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load videos"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        // In a real app, you would filter videos based on the selected filter
        // For now, we'll just reload all videos
        loadVideos()
    }
    
    fun refreshVideos() {
        loadVideos()
    }
    
    fun clearError() {
        _error.value = null
    }
} 