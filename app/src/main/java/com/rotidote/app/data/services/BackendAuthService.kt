package com.rotidote.app.data.services

import com.rotidote.app.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendAuthService @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val baseUrl = "https://backend-ten-lovat.vercel.app/" // Update with your backend URL
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    suspend fun signUp(email: String, password: String, name: String = "", grade: String = "", section: String = "", schoolName: String = ""): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("name", name)
                    put("grade", grade)
                    put("section", section)
                    put("schoolName", schoolName)
                }
                
                val request = Request.Builder()
                    .url("$baseUrl/auth/signup")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val userJson = jsonResponse.getJSONObject("user")
                    
                    val user = User(
                        id = userJson.getString("id"),
                        email = userJson.getString("email"),
                        name = userJson.getString("name"),
                        grade = userJson.getString("grade"),
                        section = userJson.getString("section"),
                        schoolName = userJson.getString("schoolName")
                    )
                    
                    val token = jsonResponse.getString("token")
                    val profileComplete = jsonResponse.getBoolean("profileComplete")
                    
                    Result.success(AuthResponse(user, token, profileComplete))
                } else {
                    val errorMessage = if (responseBody != null) {
                        val errorJson = JSONObject(responseBody)
                        errorJson.getString("error")
                    } else {
                        "Signup failed"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                
                val request = Request.Builder()
                    .url("$baseUrl/auth/login")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val userJson = jsonResponse.getJSONObject("user")
                    
                    val user = User(
                        id = userJson.getString("id"),
                        email = userJson.getString("email"),
                        name = userJson.getString("name"),
                        grade = userJson.getString("grade"),
                        section = userJson.getString("section"),
                        schoolName = userJson.getString("schoolName")
                    )
                    
                    val token = jsonResponse.getString("token")
                    val profileComplete = jsonResponse.getBoolean("profileComplete")
                    
                    Result.success(AuthResponse(user, token, profileComplete))
                } else {
                    val errorMessage = if (responseBody != null) {
                        val errorJson = JSONObject(responseBody)
                        errorJson.getString("error")
                    } else {
                        "Login failed"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateProfile(token: String, name: String, grade: String, section: String, schoolName: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("token", token)
                    put("name", name)
                    put("grade", grade)
                    put("section", section)
                    put("schoolName", schoolName)
                }
                
                val request = Request.Builder()
                    .url("$baseUrl/auth/profile")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val userJson = jsonResponse.getJSONObject("user")
                    
                    val user = User(
                        id = userJson.getString("id"),
                        email = userJson.getString("email"),
                        name = userJson.getString("name"),
                        grade = userJson.getString("grade"),
                        section = userJson.getString("section"),
                        schoolName = userJson.getString("schoolName")
                    )
                    
                    val profileComplete = jsonResponse.getBoolean("profileComplete")
                    
                    Result.success(AuthResponse(user, token, profileComplete))
                } else {
                    val errorMessage = if (responseBody != null) {
                        val errorJson = JSONObject(responseBody)
                        errorJson.getString("error")
                    } else {
                        "Profile update failed"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun verifyToken(token: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/auth/verify")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val userJson = jsonResponse.getJSONObject("user")
                    
                    val user = User(
                        id = userJson.getString("id"),
                        email = userJson.getString("email"),
                        name = userJson.getString("name"),
                        grade = userJson.getString("grade"),
                        section = userJson.getString("section"),
                        schoolName = userJson.getString("schoolName")
                    )
                    
                    val profileComplete = jsonResponse.getBoolean("profileComplete")
                    
                    Result.success(AuthResponse(user, token, profileComplete))
                } else {
                    val errorMessage = if (responseBody != null) {
                        val errorJson = JSONObject(responseBody)
                        errorJson.getString("error")
                    } else {
                        "Token verification failed"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    data class AuthResponse(
        val user: User,
        val token: String,
        val profileComplete: Boolean
    )
}

