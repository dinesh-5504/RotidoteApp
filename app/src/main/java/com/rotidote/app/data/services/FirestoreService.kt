package com.rotidote.app.data.services

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.Video
import com.rotidote.app.data.models.Session
import com.rotidote.app.data.models.AdminVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveVideo(video: Video): Result<Unit> {
        return try {
            // Generate a new document ID if not provided
            val documentId = if (video.videoId.isNotEmpty()) video.videoId else firestore.collection("videos").document().id
            val videoWithId = video.copy(videoId = documentId)
            
            firestore.collection("videos")
                .document(documentId)
                .set(videoWithId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideo(videoId: String): Video? {
        Log.d("FirestoreService", "Fetching video with ID: $videoId")
        return try {
            val document = firestore.collection("videos")
                .document(videoId)
                .get()
                .await()
            
            if (document.exists()) {
                val video = document.toObject(Video::class.java)
                Log.d("FirestoreService", "Video found: ${video?.title}, mainVideo.playbackId: ${video?.mainVideo?.playbackId}, adVideo.playbackId: ${video?.adVideo?.playbackId}")
                video
            } else {
                Log.e("FirestoreService", "Video document does not exist for ID: $videoId")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fetching video: ${e.message}", e)
            null
        }
    }

    fun getVideosStream(): Flow<List<Video>> {
        return firestore.collection("videos")
            .orderBy("videoId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject(Video::class.java)
                }
            }
    }

    suspend fun getVideos(limit: Int): List<Video> {
        return try {
            val querySnapshot = firestore.collection("videos")
                .orderBy("videoId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Video::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVideosByGenre(genre: String): List<Video> {
        return try {
            val querySnapshot = firestore.collection("videos")
                .whereEqualTo("mainGenre", genre)
                .orderBy("videoId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Video::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVideosByAdGenre(adGenre: String): List<Video> {
        return try {
            val querySnapshot = firestore.collection("videos")
                .whereEqualTo("adGenre", adGenre)
                .orderBy("videoId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Video::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Admin-specific functions
    suspend fun getAllUsers(): List<User> {
        return try {
            val querySnapshot = firestore.collection("users")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsersBySchool(school: String): List<User> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("schoolName", school)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsersByGrade(grade: String): List<User> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("grade", grade)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchUsersByName(name: String): List<User> {
        return try {
            val querySnapshot = firestore.collection("users")
                .orderBy("name")
                .startAt(name)
                .endAt(name + "\uf8ff")
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Session management
    suspend fun saveSession(session: Session): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(session.dayId)
                .set(session)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSession(dayId: String): Session? {
        return try {
            val document = firestore.collection("sessions")
                .document(dayId)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Session::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllSessions(): List<Session> {
        return try {
            val querySnapshot = firestore.collection("sessions")
                .orderBy("dayId")
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Session::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateSessionEnabled(dayId: String, enabled: Boolean): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(dayId)
                .update("enabled", enabled)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSessionPermittedStudents(dayId: String, permittedStudents: List<String>): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(dayId)
                .update("permittedStudents", permittedStudents)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin video management
    suspend fun saveAdminVideo(adminVideo: AdminVideo): Result<Unit> {
        return try {
            val documentId = if (adminVideo.videoId.isNotEmpty()) adminVideo.videoId else firestore.collection("videos").document().id
            val videoWithId = adminVideo.copy(videoId = documentId)
            
            firestore.collection("videos")
                .document(documentId)
                .set(videoWithId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideosByDay(dayId: String): List<AdminVideo> {
        return try {
            val querySnapshot = firestore.collection("videos")
                .whereEqualTo("assignedDay", dayId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(AdminVideo::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

