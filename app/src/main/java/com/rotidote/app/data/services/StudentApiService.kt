package com.rotidote.app.data.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentApiService @Inject constructor(
    private val auth: FirebaseAuth
) {
    
    private val baseUrl = "https://rotidoteapp.onrender.com"
    
    private suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("StudentApiService", "Failed to get auth token", e)
            null
        }
    }
    
    private suspend fun makeRequest(
        endpoint: String,
        method: String = "GET"
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
            Log.e("StudentApiService", "API request failed", e)
            ApiResponse(false, error = e.message ?: "Network error")
        }
    }
    
    suspend fun getPermittedDays(): ApiResponse<List<PermittedDay>> {
        val response = makeRequest("/students/permitted-days")
        return if (response.success && response.data != null) {
            try {
                val permittedDaysArray = response.data.getJSONArray("permittedDays")
                val permittedDays = mutableListOf<PermittedDay>()
                
                for (i in 0 until permittedDaysArray.length()) {
                    val dayObj = permittedDaysArray.getJSONObject(i)
                    permittedDays.add(
                        PermittedDay(
                            dayId = dayObj.getString("dayId"),
                            title = dayObj.getString("title"),
                            videoCount = dayObj.getInt("videoCount")
                        )
                    )
                }
                
                ApiResponse(true, permittedDays)
            } catch (e: Exception) {
                Log.e("StudentApiService", "Error parsing permitted days", e)
                ApiResponse(false, error = "Failed to parse response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
    
    suspend fun getVideosForDay(dayId: String): ApiResponse<List<Video>> {
        val response = makeRequest("/students/videos/$dayId")
        return if (response.success && response.data != null) {
            try {
                val videosArray = response.data.getJSONArray("videos")
                val videos = mutableListOf<Video>()
                
                for (i in 0 until videosArray.length()) {
                    val videoObj = videosArray.getJSONObject(i)
                    videos.add(
                        Video(
                            id = videoObj.getString("id"),
                            title = videoObj.getString("title"),
                            duration = videoObj.getInt("duration"),
                            muxPlaybackId = videoObj.optString("muxPlaybackId", ""),
                            muxUrl = videoObj.optString("muxUrl", ""),
                            thumbnailUrl = videoObj.optString("thumbnailUrl", ""),
                            assignedDay = videoObj.getString("assignedDay")
                        )
                    )
                }
                
                ApiResponse(true, videos)
            } catch (e: Exception) {
                Log.e("StudentApiService", "Error parsing videos", e)
                ApiResponse(false, error = "Failed to parse response")
            }
        } else {
            ApiResponse(false, error = response.error)
        }
    }
}

data class PermittedDay(
    val dayId: String,
    val title: String,
    val videoCount: Int
)

data class Video(
    val id: String,
    val title: String,
    val duration: Int,
    val muxPlaybackId: String,
    val muxUrl: String,
    val thumbnailUrl: String,
    val assignedDay: String
)
