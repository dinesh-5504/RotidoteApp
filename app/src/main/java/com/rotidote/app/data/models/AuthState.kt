package com.rotidote.app.data.models

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object ProfileIncomplete : AuthState()
    data class Error(val message: String) : AuthState()
} 