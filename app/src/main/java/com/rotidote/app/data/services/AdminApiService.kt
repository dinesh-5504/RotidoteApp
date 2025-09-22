package com.rotidote.app.data.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.Session
import com.rotidote.app.data.models.AdminVideo
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

data class StudentsResponse(
    val students: List<User>,
    val total: Int
)

data class SessionsResponse(
    val sessions: List<Session>
)

data class AnalyticsResponse(
    val totalUsers: Int,
    val totalVideos: Int,
    val enabledSessions: Int,
    val lastUpdated: String
)

data class UploadResponse(
    val message: String,
    val videoId: String,
    val video: AdminVideo
)

@Singleton
class AdminApiService @Inject constructor(
    private val auth: FirebaseAuth
) {
    
    private val baseUrl = "https://rotidoteapp.onrender.com" // Replace with actual backend URL
    
    private suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("AdminApiService", "Failed to get auth token", e)
            null
        }
    }
    
    private suspend fun makeRequest(
        endpoint: String,
        method: String = "GET",
        body: JSONObject? = null
    ): ApiResponse<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val token = getAuthToken() ?: return@withContext ApiResponse(false, error = "No auth token")
            
            val url = URL("$baseUrl$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            // Send body if provided
            if (body != null && (method == "POST" || method == "PATCH")) {
                connection.doOutput = true
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(body.toString())
                writer.flush()
                writer.close()
            }
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readText()
            reader.close()
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                ApiResponse(true, JSONObject(response))
            } else {
                val errorObj = JSONObject(response)
                ApiResponse(false, error = errorObj.optString("error", "Unknown error"))
            }
            
        } catch (e: Exception) {
            Log.e("AdminApiService", "API request failed", e)
            ApiResponse(false, error = e.message ?: "Network error")
        }
    }
    
    // Students API
    suspend fun getStudents(
        school: String? = null,
        grade: String? = null,
        section: String? = null,
        search: String? = null
    ): ApiResponse<StudentsResponse> {
        val params = mutableListOf<String>()
        school?.let { params.add("school=$it") }
        grade?.let { params.add("grade=$it") }
        section?.let { params.add("section=$it") }
        search?.let { params.add("search=$it") }
        
        val queryString = if (params.isNotEmpty()) "?" + params.joinToString("&") else ""
        val endpoint = "/admin/students$queryString"
        
        val response = makeRequest(endpoint)
        
        return if (response.success && response.data != null) {
            try {
                val studentsArray = response.data.getJSONArray("students")
                val students = mutableListOf<User>()
                
                for (i in 0 until studentsArray.length()) {
                    val studentObj = studentsArray.getJSONObject(i)
                    val user = User(
                        id = studentObj.getString("id"),
                        email = studentObj.getString("email"),
                        name = studentObj.getString("name"),
                        grade = studentObj.getString("grade"),
                        section = studentObj.getString("section"),
                        schoolName = studentObj.getString("schoolName"),
                        createdAt = studentObj.optLong("createdAt", 0),
                        updatedAt = studentObj.optLong("updatedAt", 0)
                    )
                    students.add(user)
                }
                
                ApiResponse(true, StudentsResponse(students, response.data.getInt("total")))
            } catch (e: Exception) {
                ApiResponse(false, error = "Failed to parse students response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    // Sessions API
    suspend fun getSessions(): ApiResponse<SessionsResponse> {
        val response = makeRequest("/admin/sessions")
        
        return if (response.success && response.data != null) {
            try {
                val sessionsArray = response.data.getJSONArray("sessions")
                val sessions = mutableListOf<Session>()
                
                for (i in 0 until sessionsArray.length()) {
                    val sessionObj = sessionsArray.getJSONObject(i)
                    val session = Session(
                        dayId = sessionObj.getString("dayId"),
                        title = sessionObj.getString("title"),
                        enabled = sessionObj.getBoolean("enabled"),
                        videos = jsonArrayToStringList(sessionObj.optJSONArray("videos")),
                        permittedStudents = jsonArrayToStringList(sessionObj.optJSONArray("permittedStudents")),
                        createdAt = sessionObj.optLong("createdAt", 0)
                    )
                    sessions.add(session)
                }
                
                ApiResponse(true, SessionsResponse(sessions))
            } catch (e: Exception) {
                ApiResponse(false, error = "Failed to parse sessions response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    suspend fun createOrUpdateSession(
        dayId: String,
        title: String,
        enabled: Boolean,
        videos: List<String>,
        permittedStudents: List<String>
    ): ApiResponse<String> {
        val body = JSONObject().apply {
            put("title", title)
            put("enabled", enabled)
            put("videos", JSONArray(videos))
            put("permittedStudents", JSONArray(permittedStudents))
        }
        
        val response = makeRequest("/admin/sessions/$dayId", "POST", body)
        
        return if (response.success) {
            ApiResponse(true, "Session updated successfully")
        } else {
            ApiResponse(false, error = response.error)
        }
    }

    suspend fun toggleSession(dayId: String, enabled: Boolean): ApiResponse<String> {
        val body = JSONObject().apply {
            put("enabled", enabled)
        }
        
        val response = makeRequest("/admin/sessions/$dayId/toggle", "PATCH", body)
        
        return if (response.success) {
            ApiResponse(true, "Session updated successfully")
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    suspend fun updateSessionStudents(dayId: String, permittedStudents: List<String>): ApiResponse<String> {
        val body = JSONObject().apply {
            put("permittedStudents", JSONArray(permittedStudents))
        }
        
        val response = makeRequest("/admin/sessions/$dayId/students", "PATCH", body)
        
        return if (response.success) {
            ApiResponse(true, "Students updated successfully")
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    // Upload API
    suspend fun uploadVideo(
        title: String,
        description: String,
        duration: Int,
        muxPlaybackId: String,
        thumbnailUrl: String,
        assignedDay: String
    ): ApiResponse<UploadResponse> {
        val body = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("duration", duration)
            put("muxPlaybackId", muxPlaybackId)
            put("thumbnailUrl", thumbnailUrl)
            put("assignedDay", assignedDay)
        }
        
        val response = makeRequest("/admin/upload", "POST", body)
        
        return if (response.success && response.data != null) {
            try {
                val message = response.data.getString("message")
                val videoId = response.data.getString("videoId")
                val videoObj = response.data.getJSONObject("video")
                
                val video = AdminVideo(
                    videoId = videoId,
                    title = videoObj.getString("title"),
                    description = videoObj.getString("description"),
                    duration = videoObj.getInt("duration"),
                    muxPlaybackId = videoObj.getString("muxPlaybackId"),
                    assignedDay = videoObj.getString("assignedDay"),
                    createdAt = videoObj.optLong("createdAt", 0)
                )
                
                ApiResponse(true, UploadResponse(message, videoId, video))
            } catch (e: Exception) {
                ApiResponse(false, error = "Failed to parse upload response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    // Analytics API
    suspend fun getAnalytics(): ApiResponse<AnalyticsResponse> {
        val response = makeRequest("/admin/analytics")
        
        return if (response.success && response.data != null) {
            try {
                val analyticsObj = response.data.getJSONObject("analytics")
                val analytics = AnalyticsResponse(
                    totalUsers = analyticsObj.getInt("totalUsers"),
                    totalVideos = analyticsObj.getInt("totalVideos"),
                    enabledSessions = analyticsObj.getInt("enabledSessions"),
                    lastUpdated = analyticsObj.getString("lastUpdated")
                )
                
                ApiResponse(true, analytics)
            } catch (e: Exception) {
                ApiResponse(false, error = "Failed to parse analytics response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    private fun jsonArrayToStringList(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}

