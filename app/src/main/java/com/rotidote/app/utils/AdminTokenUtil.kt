package com.rotidote.app.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminTokenUtil @Inject constructor(
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "AdminTokenUtil"
        private const val ADMIN_EMAIL = "dineshkarthikeyan.admin@gmail.com"
    }
    
    /**
     * Fetches and logs the Firebase ID token for admin users
     * Only runs for admin login (email matches admin email)
     * Logs token to console for easy copy-paste from Logcat
     */
    suspend fun fetchAdminToken() {
        Log.d(TAG, "=== STARTING ADMIN TOKEN FETCH ===")
        
        try {
            Log.d(TAG, "Checking Firebase Auth current user...")
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.w(TAG, "❌ No current user found - Firebase Auth not signed in")
                return
            }
            
            Log.d(TAG, "✅ Current user found: ${currentUser.uid}")
            Log.d(TAG, "📧 Current user email: ${currentUser.email}")
            Log.d(TAG, "🔍 Admin email expected: $ADMIN_EMAIL")
            
            // Check if current user is admin
            if (currentUser.email != ADMIN_EMAIL) {
                Log.w(TAG, "❌ Current user is not admin (${currentUser.email} != $ADMIN_EMAIL), skipping token fetch")
                return
            }
            
            Log.d(TAG, "✅ Admin user confirmed, fetching ID token...")
            
            // Force token refresh to get the latest token
            Log.d(TAG, "🔄 Calling getIdToken(true).await()...")
            val idToken = currentUser.getIdToken(true).await()
            Log.d(TAG, "✅ getIdToken() completed")
            
            val token = idToken.token
            Log.d(TAG, "🔑 Token extracted: ${token?.let { "Length: ${it.length}" } ?: "NULL"}")
            
            if (token != null && token.isNotEmpty()) {
                // Log to console for easy access from Logcat
                Log.d("AdminToken", "Token: $token")
                Log.d(TAG, "✅ Admin token fetch completed successfully")
            } else {
                Log.e(TAG, "❌ Failed to get ID token - token is null or empty")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in fetchAdminToken", e)
        }
        
        Log.d(TAG, "=== ADMIN TOKEN FETCH COMPLETED ===")
    }
    
    /**
     * Checks if the current user is an admin
     */
    fun isCurrentUserAdmin(): Boolean {
        return auth.currentUser?.email == ADMIN_EMAIL
    }
    
    /**
     * Gets the current user's email for debugging
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}
