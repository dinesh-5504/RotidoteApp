package com.rotidote.app.data.services

import com.google.firebase.firestore.FirebaseFirestore
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.Video
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
            firestore.collection("videos")
                .add(video)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideo(videoId: String): Video? {
        return try {
            val document = firestore.collection("videos")
                .document(videoId)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Video::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getVideos(limit: Int): List<Video> {
        return try {
            val querySnapshot = firestore.collection("videos")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    suspend fun updateVideoLikes(videoId: String, newLikes: Int): Result<Unit> {
        return try {
            firestore.collection("videos")
                .document(videoId)
                .update("likes", newLikes)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVideoDislikes(videoId: String, newDislikes: Int): Result<Unit> {
        return try {
            firestore.collection("videos")
                .document(videoId)
                .update("dislikes", newDislikes)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

