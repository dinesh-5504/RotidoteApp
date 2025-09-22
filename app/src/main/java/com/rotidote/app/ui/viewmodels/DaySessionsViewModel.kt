package com.rotidote.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.services.StudentApiService
import com.rotidote.app.data.services.PermittedDay
import com.rotidote.app.data.services.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaySessionsViewModel @Inject constructor(
    private val studentApiService: StudentApiService
) : ViewModel() {
    
    private val _permittedDays = MutableStateFlow<List<PermittedDay>>(emptyList())
    val permittedDays: StateFlow<List<PermittedDay>> = _permittedDays.asStateFlow()
    
    private val _selectedDay = MutableStateFlow<String?>(null)
    val selectedDay: StateFlow<String?> = _selectedDay.asStateFlow()
    
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPermittedDays()
    }
    
    fun loadPermittedDays() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("DaySessionsViewModel", "Loading permitted days...")
                val response = studentApiService.getPermittedDays()
                
                if (response.success && response.data != null) {
                    _permittedDays.value = response.data
                    Log.d("DaySessionsViewModel", "Loaded ${response.data.size} permitted days")
                    
                    // Auto-select first day if available
                    if (response.data.isNotEmpty()) {
                        selectDay(response.data.first().dayId)
                    }
                } else {
                    _error.value = response.error ?: "Failed to load permitted days"
                    Log.e("DaySessionsViewModel", "Failed to load permitted days: ${response.error}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load permitted days: ${e.message}"
                Log.e("DaySessionsViewModel", "Exception loading permitted days", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectDay(dayId: String) {
        viewModelScope.launch {
            _selectedDay.value = dayId
            loadVideosForDay(dayId)
        }
    }
    
    private fun loadVideosForDay(dayId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("DaySessionsViewModel", "Loading videos for day: $dayId")
                val response = studentApiService.getVideosForDay(dayId)
                
                if (response.success && response.data != null) {
                    _videos.value = response.data
                    Log.d("DaySessionsViewModel", "Loaded ${response.data.size} videos for $dayId")
                } else {
                    _error.value = response.error ?: "Failed to load videos"
                    Log.e("DaySessionsViewModel", "Failed to load videos: ${response.error}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load videos: ${e.message}"
                Log.e("DaySessionsViewModel", "Exception loading videos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
