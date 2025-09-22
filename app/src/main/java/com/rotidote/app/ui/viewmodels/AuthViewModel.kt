package com.rotidote.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.AuthState
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.UserType
import com.rotidote.app.data.models.AdminCredentials
import com.rotidote.app.data.services.FirebaseAuthService
import com.rotidote.app.data.services.StudentApiService
import com.rotidote.app.utils.AdminTokenUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val adminTokenUtil: AdminTokenUtil,
    private val studentApiService: StudentApiService
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _userType = MutableStateFlow<UserType?>(null)
    val userType: StateFlow<UserType?> = _userType.asStateFlow()
    
    private val _shouldFetchAdminToken = MutableStateFlow(false)
    val shouldFetchAdminToken: StateFlow<Boolean> = _shouldFetchAdminToken.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            if (authService.isUserLoggedIn()) {
                val firebaseUser = authService.currentUser
                if (firebaseUser != null) {
                    // Check if admin
                    val adminCredentials = AdminCredentials()
                    if (firebaseUser.email == adminCredentials.email) {
                        _userType.value = UserType.ADMIN
                        _authState.value = AuthState.Authenticated(UserType.ADMIN)
                        return@launch
                    }
                    
                    // Regular student user
                    val userProfile = authService.getUserProfile(firebaseUser.uid)
                    userProfile.fold(
                        onSuccess = { user ->
                            if (user != null && user.name.isNotEmpty() && user.grade.isNotEmpty() && user.section.isNotEmpty() && user.schoolName.isNotEmpty()) {
                                _currentUser.value = user
                                _userType.value = UserType.STUDENT
                                
                                // Check if student has permitted days
                                checkStudentPermittedDays()
                            } else {
                                _authState.value = AuthState.ProfileIncomplete
                            }
                        },
                        onFailure = { exception ->
                            // If profile doesn't exist, user needs to complete profile
                            _authState.value = AuthState.ProfileIncomplete
                        }
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Check if admin credentials
            val adminCredentials = AdminCredentials()
            if (email == adminCredentials.email && password == adminCredentials.password) {
                Log.d("AuthViewModel", "🔐 Attempting admin sign in...")
                val result = authService.signIn(email, password)
                result.fold(
                    onSuccess = { firebaseUser ->
                        Log.d("AuthViewModel", "✅ Admin sign in successful, Firebase user: ${firebaseUser.uid}")
                        _userType.value = UserType.ADMIN
                        _authState.value = AuthState.Authenticated(UserType.ADMIN)
                        Log.d("AuthViewModel", "🚀 Setting shouldFetchAdminToken = true")
                        // Signal that admin token should be fetched (UI layer will handle this)
                        _shouldFetchAdminToken.value = true
                    },
                    onFailure = { exception ->
                        Log.e("AuthViewModel", "❌ Admin sign in failed", exception)
                        _authState.value = AuthState.Error(exception.message ?: "Admin sign in failed")
                    }
                )
                return@launch
            }
            
            // Regular student sign in
            val result = authService.signIn(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    val userProfile = authService.getUserProfile(firebaseUser.uid)
                    userProfile.fold(
                        onSuccess = { user ->
                            if (user != null && user.name.isNotEmpty() && user.grade.isNotEmpty() && user.section.isNotEmpty() && user.schoolName.isNotEmpty()) {
                                _currentUser.value = user
                                _userType.value = UserType.STUDENT
                                
                                // Check if student has permitted days
                                checkStudentPermittedDays()
                            } else {
                                _authState.value = AuthState.ProfileIncomplete
                            }
                        },
                        onFailure = { exception ->
                            // If profile doesn't exist, user needs to complete profile
                            _authState.value = AuthState.ProfileIncomplete
                        }
                    )
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                }
            )
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authService.signUp(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    _authState.value = AuthState.ProfileIncomplete
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            )
        }
    }
    
    fun saveUserProfile(name: String, grade: String, section: String, schoolName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val firebaseUser = authService.currentUser
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = name,
                    grade = grade,
                    section = section,
                    schoolName = schoolName
                )
                val result = authService.saveUserProfile(user)
                result.fold(
                    onSuccess = {
                        _currentUser.value = user
                        _userType.value = UserType.STUDENT
                        _authState.value = AuthState.Authenticated(UserType.STUDENT)
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Failed to save profile")
                    }
                )
            } else {
                _authState.value = AuthState.Error("No authenticated user")
            }
        }
    }
    
    fun signOut() {
        authService.signOut()
        _currentUser.value = null
        _userType.value = null
        _authState.value = AuthState.Unauthenticated
        _shouldFetchAdminToken.value = false
    }
    
    /**
     * Resets the admin token fetch flag after it has been processed
     */
    fun resetAdminTokenFetchFlag() {
        _shouldFetchAdminToken.value = false
    }
    
    /**
     * Manually fetch admin token (can be called from UI)
     */
    suspend fun fetchAdminToken() {
        Log.d("AuthViewModel", "🎯 fetchAdminToken() called from UI")
        try {
            adminTokenUtil.fetchAdminToken()
            Log.d("AuthViewModel", "✅ fetchAdminToken() completed")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Exception in fetchAdminToken()", e)
        }
    }
    
    /**
     * Check if student has permitted days for day sessions
     */
    private fun checkStudentPermittedDays() {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "🔍 Checking student permitted days...")
                val response = studentApiService.getPermittedDays()
                
                if (response.success && response.data != null && response.data.isNotEmpty()) {
                    Log.d("AuthViewModel", "✅ Student has ${response.data.size} permitted days - redirecting to DaySessions")
                    _authState.value = AuthState.DaySessionsAvailable
                } else {
                    Log.d("AuthViewModel", "ℹ️ Student has no permitted days - redirecting to regular Home")
                    _authState.value = AuthState.Authenticated(UserType.STUDENT)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error checking permitted days, defaulting to regular Home", e)
                _authState.value = AuthState.Authenticated(UserType.STUDENT)
            }
        }
    }
} 