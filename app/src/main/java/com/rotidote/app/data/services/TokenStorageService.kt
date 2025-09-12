package com.rotidote.app.data.services

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }
    
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun saveUserInfo(userId: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }
    
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun clearAuthData() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}

