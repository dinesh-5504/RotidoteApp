package com.rotidote.app.data.models

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userType: UserType) : AuthState()
    object Unauthenticated : AuthState()
    object ProfileIncomplete : AuthState()
    object DaySessionsAvailable : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class UserType {
    STUDENT,
    ADMIN
} 