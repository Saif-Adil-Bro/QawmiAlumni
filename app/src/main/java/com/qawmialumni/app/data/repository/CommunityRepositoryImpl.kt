package com.qawmialumni.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.qawmialumni.app.core.network.ImageUploadRepository
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.remote.dto.ChatMessageDto
import com.qawmialumni.app.data.remote.dto.ChatRoomDto
import com.qawmialumni.app.data.remote.dto.ForumPostDto
import com.qawmialumni.app.data.remote.dto.PostCommentDto
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.repository.CommunityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageUploadRepository: ImageUploadRepository,
) : CommunityRepository {

    override fun getForumPosts(
        madrasaId: String,
        category: ForumCategory?,
    ): Flow<Resource<List<ForumPost>>> = callbackFlow {
        trySend(Resource.Loading)
        var query: Query = firestore.collection("forum_posts")
            .whereEqualTo("madrasaId", madrasaId)

        if (category != null) {
            query = query.whereEqualTo("category", category.wireValue)
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING).limit(30)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "পোস্ট লোড ত্রুটি", error))
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(ForumPostDto::class.java)?.toDomain() }
                ?: emptyList()
            trySend(Resource.Success(list))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createPost(post: ForumPost, imageUri: Uri?): Resource<Unit> {
        return try {
            var imageUrl = post.imageUrl
            if (imageUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(imageUri, ImageTarget.FULL)) {
                    is Resource.Success -> imageUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val postId = post.postId.ifBlank { "POST_${System.currentTimeMillis()}" }
            val finalPost = post.copy(
                postId = postId,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis(),
            )

            firestore.collection("forum_posts").document(postId)
                .set(ForumPostDto.fromDomain(finalPost))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "পোস্ট প্রকাশ ব্যর্থ", e)
        }
    }

    override suspend fun toggleLikePost(postId: String, currentLikes: Int): Resource<Unit> {
        return try {
            firestore.collection("forum_posts").document(postId)
                .update("likesCount", FieldValue.increment(1))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "লাইক ব্যর্থ", e)
        }
    }

    override fun getComments(postId: String): Flow<Resource<List<PostComment>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("forum_posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "মন্তব্য লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(PostCommentDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(postId: String, comment: PostComment): Resource<Unit> {
        return try {
            val commentId = comment.commentId.ifBlank { "COMM_${System.currentTimeMillis()}" }
            val finalComment = comment.copy(commentId = commentId, createdAt = System.currentTimeMillis())

            val postRef = firestore.collection("forum_posts").document(postId)
            postRef.collection("comments").document(commentId)
                .set(PostCommentDto.fromDomain(finalComment))
                .await()

            postRef.update("commentsCount", FieldValue.increment(1)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মন্তব্য যোগ করা যায়নি", e)
        }
    }

    /**
     * Bucketed Messaging Pattern (Blueprint §5.D.2):
     * Messages array is kept directly inside the room document to prevent N-reads.
     */
    override fun getChatRooms(userId: String): Flow<Resource<List<ChatRoom>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("chat_rooms")
            .whereArrayContains("participants", userId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "চ্যাট তালিকা লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(ChatRoomDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }

    override fun observeChatRoom(roomId: String): Flow<Resource<ChatRoom>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("chat_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "রুম লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(ChatRoomDto::class.java)?.toDomain()
                if (room != null) {
                    trySend(Resource.Success(room))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        roomId: String,
        madrasaId: String,
        senderId: String,
        recipientId: String,
        senderName: String,
        recipientName: String,
        senderPhoto: String,
        recipientPhoto: String,
        text: String,
    ): Resource<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val newMsg = ChatMessageDto(senderId = senderId, text = text.trim(), timestamp = timestamp)

            val roomData = mapOf(
                "roomId" to roomId,
                "madrasaId" to madrasaId,
                "participants" to listOf(senderId, recipientId),
                "participantNames" to mapOf(senderId to senderName, recipientId to recipientName),
                "participantPhotos" to mapOf(senderId to senderPhoto, recipientId to recipientPhoto),
                "lastMessage" to text.trim(),
                "lastTimestamp" to timestamp,
            )

            val roomRef = firestore.collection("chat_rooms").document(roomId)
            roomRef.set(roomData, SetOptions.merge()).await()
            roomRef.update("recentMessages", FieldValue.arrayUnion(newMsg)).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মেসেজ পাঠানো ব্যর্থ", e)
        }
    }
}
