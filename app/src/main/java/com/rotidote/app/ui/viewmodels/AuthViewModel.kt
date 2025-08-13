package com.rotidote.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotidote.app.data.models.AuthState
import com.rotidote.app.data.models.User
import com.rotidote.app.data.services.FirebaseAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: FirebaseAuthService
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            if (authService.isUserLoggedIn()) {
                val firebaseUser = authService.currentUser
                if (firebaseUser != null) {
                    val userProfile = authService.getUserProfile(firebaseUser.uid)
                    userProfile.fold(
                        onSuccess = { user ->
                            if (user != null && user.name.isNotEmpty() && user.grade.isNotEmpty() && user.section.isNotEmpty() && user.schoolName.isNotEmpty()) {
                                _currentUser.value = user
                                _authState.value = AuthState.Authenticated
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
            val result = authService.signIn(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    val userProfile = authService.getUserProfile(firebaseUser.uid)
                    userProfile.fold(
                        onSuccess = { user ->
                            if (user != null && user.name.isNotEmpty() && user.grade.isNotEmpty() && user.section.isNotEmpty() && user.schoolName.isNotEmpty()) {
                                _currentUser.value = user
                                _authState.value = AuthState.Authenticated
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
                        _authState.value = AuthState.Authenticated
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
        _authState.value = AuthState.Unauthenticated
    }
} 