package com.rotidote.app.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rotidote.app.data.models.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreService: FirestoreService
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            firestoreService.saveUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val user = firestoreService.getUser(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
} 