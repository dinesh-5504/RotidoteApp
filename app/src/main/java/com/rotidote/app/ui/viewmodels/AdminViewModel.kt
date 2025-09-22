package com.rotidote.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.Session
import com.rotidote.app.data.models.AdminVideo
import com.rotidote.app.data.services.AdminApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminApiService: AdminApiService
) : ViewModel() {
    
    // Students management
    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()
    
    private val _filteredStudents = MutableStateFlow<List<User>>(emptyList())
    val filteredStudents: StateFlow<List<User>> = _filteredStudents.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedSchool = MutableStateFlow<String?>(null)
    val selectedSchool: StateFlow<String?> = _selectedSchool.asStateFlow()
    
    private val _selectedGrade = MutableStateFlow<String?>(null)
    val selectedGrade: StateFlow<String?> = _selectedGrade.asStateFlow()
    
    private val _selectedSection = MutableStateFlow<String?>(null)
    val selectedSection: StateFlow<String?> = _selectedSection.asStateFlow()
    
    // Sessions management
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()
    
    // Video management
    private val _uploadTitle = MutableStateFlow("")
    val uploadTitle: StateFlow<String> = _uploadTitle.asStateFlow()
    
    private val _uploadDescription = MutableStateFlow("")
    val uploadDescription: StateFlow<String> = _uploadDescription.asStateFlow()
    
    private val _uploadDuration = MutableStateFlow(0)
    val uploadDuration: StateFlow<Int> = _uploadDuration.asStateFlow()
    
    private val _selectedDay = MutableStateFlow("Day1")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()
    
    // Session detail management
    private val _selectedSession = MutableStateFlow<Session?>(null)
    val selectedSession: StateFlow<Session?> = _selectedSession.asStateFlow()
    
    private val _sessionStudents = MutableStateFlow<List<User>>(emptyList())
    val sessionStudents: StateFlow<List<User>> = _sessionStudents.asStateFlow()
    
    // File upload states
    private val _selectedVideoFile = MutableStateFlow<String?>(null)
    val selectedVideoFile: StateFlow<String?> = _selectedVideoFile.asStateFlow()
    
    private val _selectedThumbnailFile = MutableStateFlow<String?>(null)
    val selectedThumbnailFile: StateFlow<String?> = _selectedThumbnailFile.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadAllData()
    }
    
    private fun loadAllData() {
        loadStudents()
        loadSessions()
    }
    
    // Students functions
    fun loadStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("AdminViewModel", "Loading students...")
                // Load all students first, then apply local filtering for better UX
                val response = adminApiService.getStudents()
                if (response.success && response.data != null) {
                    Log.d("AdminViewModel", "Loaded ${response.data.students.size} students")
                    _students.value = response.data.students
                    applyFilters() // Apply current filters to the loaded data
                } else {
                    val errorMsg = response.error ?: "Failed to load students"
                    Log.e("AdminViewModel", "Failed to load students: $errorMsg")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to load students: ${e.message}"
                Log.e("AdminViewModel", "Exception loading students", e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    fun setSchoolFilter(school: String?) {
        _selectedSchool.value = school
        applyFilters()
    }
    
    fun setGradeFilter(grade: String?) {
        _selectedGrade.value = grade
        applyFilters()
    }
    
    fun setSectionFilter(section: String?) {
        _selectedSection.value = section
        applyFilters()
    }
    
    private fun applyFilters() {
        var filtered = _students.value
        
        // Apply search filter
        val query = _searchQuery.value
        if (query.isNotEmpty()) {
            filtered = filtered.filter { student ->
                student.name.contains(query, ignoreCase = true) ||
                student.email.contains(query, ignoreCase = true)
            }
        }
        
        // Apply school filter
        _selectedSchool.value?.let { school ->
            filtered = filtered.filter { it.schoolName == school }
        }
        
        // Apply grade filter
        _selectedGrade.value?.let { grade ->
            filtered = filtered.filter { it.grade == grade }
        }
        
        // Apply section filter
        _selectedSection.value?.let { section ->
            filtered = filtered.filter { it.section == section }
        }
        
        _filteredStudents.value = filtered
    }
    
    fun getUniqueSchools(): List<String> {
        return _students.value.map { it.schoolName }.distinct().sorted()
    }
    
    fun getUniqueGrades(): List<String> {
        return _students.value.map { it.grade }.distinct().sorted()
    }
    
    fun getUniqueSections(): List<String> {
        return _students.value.map { it.section }.distinct().sorted()
    }
    
    // Sessions functions
    fun loadSessions() {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Loading sessions...")
                val response = adminApiService.getSessions()
                if (response.success && response.data != null) {
                    Log.d("AdminViewModel", "Loaded ${response.data.sessions.size} sessions")
                    _sessions.value = response.data.sessions
                } else {
                    val errorMsg = response.error ?: "Failed to load sessions"
                    Log.e("AdminViewModel", "Failed to load sessions: $errorMsg")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to load sessions: ${e.message}"
                Log.e("AdminViewModel", "Exception loading sessions", e)
                _error.value = errorMsg
            }
        }
    }
    
    fun toggleSessionEnabled(dayId: String, enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("AdminViewModel", "Toggling session $dayId to enabled: $enabled")
                
                // First create/update the session, then toggle it
                val createResponse = adminApiService.createOrUpdateSession(
                    dayId = dayId,
                    title = dayId,
                    enabled = enabled,
                    videos = emptyList(),
                    permittedStudents = emptyList()
                )
                
                if (createResponse.success) {
                    Log.d("AdminViewModel", "Session $dayId updated successfully")
                    loadSessions() // Reload to reflect changes
                } else {
                    val errorMsg = createResponse.error ?: "Failed to update session"
                    Log.e("AdminViewModel", "Failed to update session $dayId: $errorMsg")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to update session: ${e.message}"
                Log.e("AdminViewModel", "Exception updating session $dayId", e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePermittedStudents(dayId: String, studentIds: List<String>) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Updating permitted students for $dayId: ${studentIds.size} students")
                val response = adminApiService.updateSessionStudents(dayId, studentIds)
                if (response.success) {
                    Log.d("AdminViewModel", "Successfully updated permitted students, reloading sessions")
                    loadSessions() // Reload to reflect changes
                    
                    // Also refresh session students if this is the currently selected session
                    val selectedSession = _selectedSession.value
                    if (selectedSession?.dayId == dayId) {
                        loadSessionStudents(studentIds)
                    }
                } else {
                    _error.value = response.error ?: "Failed to update permitted students"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update permitted students: ${e.message}"
            }
        }
    }
    
    // Video upload functions
    fun setUploadTitle(title: String) {
        _uploadTitle.value = title
    }
    
    fun setUploadDescription(description: String) {
        _uploadDescription.value = description
    }
    
    fun setUploadDuration(duration: Int) {
        _uploadDuration.value = duration
    }
    
    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }
    
    fun uploadVideo(muxPlaybackId: String, thumbnailUrl: String, duration: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = adminApiService.uploadVideo(
                    title = _uploadTitle.value,
                    description = _uploadDescription.value,
                    duration = duration,
                    muxPlaybackId = muxPlaybackId,
                    thumbnailUrl = thumbnailUrl,
                    assignedDay = _selectedDay.value
                )
                
                if (response.success) {
                    Log.d("AdminViewModel", "Video uploaded successfully, refreshing sessions")
                    // Clear form
                    _uploadTitle.value = ""
                    _uploadDescription.value = ""
                    _uploadDuration.value = 0
                    
                    // Reload sessions to reflect new video
                    loadSessions()
                } else {
                    _error.value = response.error ?: "Failed to upload video"
                }
            } catch (e: Exception) {
                _error.value = "Failed to upload video: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Session detail management functions
    fun selectSession(session: Session) {
        _selectedSession.value = session
        loadSessionStudents(session.permittedStudents)
    }
    
    private fun loadSessionStudents(studentIds: List<String>) {
        viewModelScope.launch {
            try {
                // Load all students and filter by IDs
                val response = adminApiService.getStudents()
                if (response.success && response.data != null) {
                    val allStudents = response.data.students
                    val sessionStudents = allStudents.filter { studentIds.contains(it.id) }
                    _sessionStudents.value = sessionStudents
                }
            } catch (e: Exception) {
                _error.value = "Failed to load session students: ${e.message}"
            }
        }
    }
    
    fun addStudentToSession(dayId: String, studentId: String) {
        viewModelScope.launch {
            try {
                // Get the latest session data from the sessions list to ensure we have current permittedStudents
                val currentSession = _sessions.value.find { it.dayId == dayId }
                if (currentSession != null) {
                    // Check if student is already permitted (deduplication)
                    if (currentSession.permittedStudents.contains(studentId)) {
                        Log.d("AdminViewModel", "Student $studentId is already permitted for $dayId")
                        return@launch
                    }
                    
                    val updatedStudents = currentSession.permittedStudents + studentId
                    Log.d("AdminViewModel", "Adding student $studentId to $dayId. New count: ${updatedStudents.size}")
                    updatePermittedStudents(dayId, updatedStudents)
                } else {
                    _error.value = "Session not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add student: ${e.message}"
            }
        }
    }
    
    fun removeStudentFromSession(dayId: String, studentId: String) {
        viewModelScope.launch {
            try {
                // Get the latest session data from the sessions list to ensure we have current permittedStudents
                val currentSession = _sessions.value.find { it.dayId == dayId }
                if (currentSession != null) {
                    val updatedStudents = currentSession.permittedStudents - studentId
                    Log.d("AdminViewModel", "Removing student $studentId from $dayId. New count: ${updatedStudents.size}")
                    updatePermittedStudents(dayId, updatedStudents)
                } else {
                    _error.value = "Session not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to remove student: ${e.message}"
            }
        }
    }
    
    // File upload functions
    fun setSelectedVideoFile(filePath: String?) {
        _selectedVideoFile.value = filePath
    }
    
    fun setSelectedThumbnailFile(filePath: String?) {
        _selectedThumbnailFile.value = filePath
    }
    
    fun uploadVideoWithFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            _uploadProgress.value = 0f
            
            try {
                val videoFile = _selectedVideoFile.value
                val thumbnailFile = _selectedThumbnailFile.value
                
                if (videoFile == null) {
                    _error.value = "Please select a video file"
                    return@launch
                }
                
                if (thumbnailFile == null) {
                    _error.value = "Please select a thumbnail image"
                    return@launch
                }
                
                // Simulate upload progress
                _uploadProgress.value = 0.2f
                
                // TODO: Implement actual file upload to Mux and Cloudinary
                // For now, using placeholder values
                val muxPlaybackId = "placeholder_mux_${System.currentTimeMillis()}"
                val thumbnailUrl = "placeholder_thumbnail_${System.currentTimeMillis()}"
                val duration = _uploadDuration.value // use actual duration from UI
                
                _uploadProgress.value = 0.8f
                
                // Upload metadata to backend
                uploadVideo(muxPlaybackId, thumbnailUrl, duration)
                
                _uploadProgress.value = 1.0f
                
                // Clear file selections
                _selectedVideoFile.value = null
                _selectedThumbnailFile.value = null
                
            } catch (e: Exception) {
                _error.value = "Failed to upload files: ${e.message}"
            } finally {
                _isLoading.value = false
                _uploadProgress.value = 0f
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

