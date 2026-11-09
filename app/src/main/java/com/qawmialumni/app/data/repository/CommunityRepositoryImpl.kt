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
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.domain.model.AuditActionType
import com.qawmialumni.app.domain.model.AuditLog
import com.qawmialumni.app.domain.repository.AdminRepository
import com.qawmialumni.app.domain.repository.CommunityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageUploadRepository: ImageUploadRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val notificationHelper: NotificationHelper,
    private val adminRepository: dagger.Lazy<AdminRepository>,
) : CommunityRepository {

    private val localPostsFallback = mutableMapOf<String, ForumPost>()

    override fun getForumPosts(
        madrasaId: String,
        category: ForumCategory?,
        includePendingForUid: String,
    ): Flow<Resource<List<ForumPost>>> = callbackFlow {
        trySend(Resource.Loading)
        // Single-field equality filter ONLY — prevents FAILED_PRECONDITION (composite index error)!
        val query = firestore.collection("forum_posts")
            .whereEqualTo("madrasaId", madrasaId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val fallbackList = localPostsFallback.values
                    .filter {
                        it.madrasaId == madrasaId &&
                            (category == null || it.category == category) &&
                            (it.approvalStatus == VerificationStatus.APPROVED ||
                                (includePendingForUid.isNotBlank() && it.authorUid == includePendingForUid))
                    }
                    .sortedWith(compareByDescending<ForumPost> { it.isPinned }.thenByDescending { it.createdAt })
                trySend(Resource.Success(fallbackList))
                return@addSnapshotListener
            }
            val remoteList = snapshot?.documents
                ?.mapNotNull { it.toObject(ForumPostDto::class.java)?.toDomain() }
                .orEmpty()
            remoteList.forEach { localPostsFallback[it.postId] = it }

            val merged = (remoteList + localPostsFallback.values.filter { it.madrasaId == madrasaId })
                .distinctBy { it.postId }
                .filter {
                    (category == null || it.category == category) &&
                        (it.approvalStatus == VerificationStatus.APPROVED ||
                            (includePendingForUid.isNotBlank() && it.authorUid == includePendingForUid))
                }
                .sortedWith(compareByDescending<ForumPost> { it.isPinned }.thenByDescending { it.createdAt })
                .take(50)
            trySend(Resource.Success(merged))
        }
        awaitClose { listener.remove() }
    }

    override fun getAllGlobalPostsForAdmin(): Flow<Resource<List<ForumPost>>> = callbackFlow {
        trySend(Resource.Loading)
        val query = firestore.collection("forum_posts")
            .whereEqualTo("madrasaId", ForumPost.GLOBAL_FORUM_ID)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val fallback = localPostsFallback.values
                    .filter { it.madrasaId == ForumPost.GLOBAL_FORUM_ID }
                    .sortedWith(
                        compareByDescending<ForumPost> { it.approvalStatus == VerificationStatus.PENDING }
                            .thenByDescending { it.isPinned }
                            .thenByDescending { it.createdAt }
                    )
                trySend(Resource.Success(fallback))
                return@addSnapshotListener
            }
            val remoteList = snapshot?.documents
                ?.mapNotNull { it.toObject(ForumPostDto::class.java)?.toDomain() }
                .orEmpty()
            remoteList.forEach { localPostsFallback[it.postId] = it }

            val merged = (remoteList + localPostsFallback.values.filter { it.madrasaId == ForumPost.GLOBAL_FORUM_ID })
                .distinctBy { it.postId }
                .sortedWith(
                    compareByDescending<ForumPost> { it.approvalStatus == VerificationStatus.PENDING }
                        .thenByDescending { it.isPinned }
                        .thenByDescending { it.createdAt }
                )
            trySend(Resource.Success(merged))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun approveGlobalPost(postId: String): Resource<Unit> {
        return try {
            localPostsFallback[postId]?.let {
                localPostsFallback[postId] = it.copy(approvalStatus = VerificationStatus.APPROVED)
            }
            runCatching {
                firestore.collection("forum_posts").document(postId)
                    .update("approvalStatus", VerificationStatus.APPROVED.wireValue)
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                val post = localPostsFallback[postId]
                adminRepository.get().recordAuditLog(
                    AuditLog(
                        madrasaId = ForumPost.GLOBAL_FORUM_ID,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.FORUM_POST_APPROVED,
                        targetTitle = post?.title ?: "পোস্ট ($postId)",
                        details = "গ্লোবাল ফোরামের পোস্ট অনুমোদন করা হয়েছে",
                    )
                )
                if (post != null) {
                    // The post owner gets the moderation result, not the approver.
                    notificationHelper.publishDirectNotification(
                        recipientUid = post.authorUid,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        title = "✅ আপনার ফোরাম পোস্ট অনুমোদিত হয়েছে",
                        message = post.title,
                        targetId = postId,
                        type = com.qawmialumni.app.core.notification.NotificationType.FORUM,
                    )
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অনুমোদন ব্যর্থ", e)
        }
    }

    override suspend fun updateForumPost(
        postId: String,
        title: String,
        content: String,
        category: ForumCategory,
        contactPhone: String,
    ): Resource<Unit> {
        return try {
            localPostsFallback[postId]?.let { existing ->
                localPostsFallback[postId] = existing.copy(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    contactPhone = contactPhone.trim(),
                )
            }
            runCatching {
                firestore.collection("forum_posts").document(postId)
                    .update(
                        mapOf(
                            "title" to title.trim(),
                            "content" to content.trim(),
                            "category" to category.wireValue,
                            "contactPhone" to contactPhone.trim(),
                        )
                    )
                    .await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "পোস্ট আপডেট ব্যর্থ", e)
        }
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

            localPostsFallback[postId] = finalPost
            runCatching {
                firestore.collection("forum_posts").document(postId)
                    .set(ForumPostDto.fromDomain(finalPost))
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                adminRepository.get().recordAuditLog(
                    AuditLog(
                        madrasaId = finalPost.madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { finalPost.authorName },
                        actorRole = session.role,
                        actionType = AuditActionType.FORUM_POST_CREATED,
                        targetTitle = finalPost.title,
                        details = "ফোরামে নতুন পোস্ট তৈরি করা হয়েছে (${finalPost.category.banglaName})",
                    )
                )

                if (finalPost.approvalStatus == VerificationStatus.APPROVED) {
                    // Notify the community (everyone except the author) — not the author himself.
                    notificationHelper.publishForumNotification(
                        madrasaId = finalPost.madrasaId,
                        authorUid = finalPost.authorUid.ifBlank { session.uid },
                        authorName = finalPost.authorName,
                        title = finalPost.title,
                        postId = postId,
                    )
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "পোস্ট প্রকাশ ব্যর্থ", e)
        }
    }

    override suspend fun toggleLikePost(
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean,
    ): Resource<Unit> {
        return try {
            val postRef = firestore.collection("forum_posts").document(postId)
            val updates = if (isCurrentlyLiked) {
                mapOf(
                    "likedByUids" to FieldValue.arrayRemove(userId),
                    "likesCount" to FieldValue.increment(-1),
                )
            } else {
                mapOf(
                    "likedByUids" to FieldValue.arrayUnion(userId),
                    "likesCount" to FieldValue.increment(1),
                )
            }
            runCatching { postRef.update(updates).await() }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "লাইক ব্যর্থ", e)
        }
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            val post = localPostsFallback[postId]
            localPostsFallback.remove(postId)
            runCatching {
                firestore.collection("forum_posts").document(postId).delete().await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                adminRepository.get().recordAuditLog(
                    AuditLog(
                        madrasaId = post?.madrasaId.orEmpty(),
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.FORUM_POST_DELETED,
                        targetTitle = post?.title ?: "পোস্ট ($postId)",
                        details = "ফোরাম পোস্ট মুছে ফেলা হয়েছে",
                    )
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "পোস্ট মুছে ফেলা যায়নি", e)
        }
    }

    override suspend fun togglePinPost(postId: String, isPinned: Boolean): Resource<Unit> {
        return try {
            val post = localPostsFallback[postId]
            localPostsFallback[postId]?.let {
                localPostsFallback[postId] = it.copy(isPinned = isPinned)
            }
            runCatching {
                firestore.collection("forum_posts").document(postId)
                    .update("isPinned", isPinned)
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                adminRepository.get().recordAuditLog(
                    AuditLog(
                        madrasaId = post?.madrasaId.orEmpty(),
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.FORUM_POST_PINNED,
                        targetTitle = post?.title ?: "পোস্ট ($postId)",
                        details = if (isPinned) "ফোরাম পোস্ট পিন করা হয়েছে" else "ফোরাম পোস্ট আনপিন করা হয়েছে",
                    )
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "পিন স্ট্যাটাস পরিবর্তন ব্যর্থ", e)
        }
    }

    private val localCommentsFallback = mutableMapOf<String, MutableList<PostComment>>()

    override fun getComments(postId: String): Flow<Resource<List<PostComment>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("forum_posts").document(postId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val fallback = localCommentsFallback[postId].orEmpty().sortedBy { it.createdAt }
                    trySend(Resource.Success(fallback))
                    return@addSnapshotListener
                }
                val remoteList = snapshot?.documents
                    ?.mapNotNull { it.toObject(PostCommentDto::class.java)?.toDomain() }
                    .orEmpty()
                val localList = localCommentsFallback[postId].orEmpty()
                val merged = (remoteList + localList)
                    .distinctBy { it.commentId }
                    .sortedBy { it.createdAt }
                localCommentsFallback[postId] = merged.toMutableList()
                trySend(Resource.Success(merged))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(postId: String, comment: PostComment): Resource<Unit> {
        return try {
            val commentId = comment.commentId.ifBlank { "COMM_${System.currentTimeMillis()}" }
            val finalComment = comment.copy(commentId = commentId, createdAt = System.currentTimeMillis())

            val list = localCommentsFallback.getOrPut(postId) { mutableListOf() }
            list.add(finalComment)
            localPostsFallback[postId]?.let { existing ->
                localPostsFallback[postId] = existing.copy(commentsCount = existing.commentsCount + 1)
            }

            runCatching {
                val postRef = firestore.collection("forum_posts").document(postId)
                postRef.collection("comments").document(commentId)
                    .set(PostCommentDto.fromDomain(finalComment))
                    .await()
                postRef.update("commentsCount", FieldValue.increment(1)).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মন্তব্য যোগ করা যায়নি", e)
        }
    }

    private val localChatRoomsFallback = mutableMapOf<String, ChatRoom>()

    /**
     * Bucketed Messaging Pattern (Blueprint §5.D.2):
     * Messages array is kept directly inside the room document to prevent N-reads,
     * and strictly capped at the latest 50 messages to prevent Firestore 1MB document overflow!
     */
    override fun getChatRooms(userId: String): Flow<Resource<List<ChatRoom>>> = callbackFlow {
        trySend(Resource.Loading)
        // Single-field array-contains filter ONLY — prevents FAILED_PRECONDITION (composite index error)!
        val listener = firestore.collection("chat_rooms")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val fallback = localChatRoomsFallback.values
                        .filter { userId in it.participants }
                        .sortedByDescending { it.lastTimestamp }
                    trySend(Resource.Success(fallback))
                    return@addSnapshotListener
                }
                val remoteList = snapshot?.documents
                    ?.mapNotNull { it.toObject(ChatRoomDto::class.java)?.toDomain() }
                    .orEmpty()
                remoteList.forEach { localChatRoomsFallback[it.roomId] = it }

                val merged = (remoteList + localChatRoomsFallback.values.filter { userId in it.participants })
                    .distinctBy { it.roomId }
                    .sortedByDescending { it.lastTimestamp }
                trySend(Resource.Success(merged))
            }
        awaitClose { listener.remove() }
    }

    override fun observeChatRoom(roomId: String): Flow<Resource<ChatRoom>> = callbackFlow {
        trySend(Resource.Loading)
        localChatRoomsFallback[roomId]?.let { trySend(Resource.Success(it)) }
        val listener = firestore.collection("chat_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val cached = localChatRoomsFallback[roomId]
                    if (cached != null) {
                        trySend(Resource.Success(cached))
                    } else {
                        trySend(Resource.Error(error.localizedMessage ?: "রুম লোড ত্রুটি", error))
                    }
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(ChatRoomDto::class.java)?.toDomain()
                if (room != null) {
                    localChatRoomsFallback[roomId] = room
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

            val roomRef = firestore.collection("chat_rooms").document(roomId)
            val existingDoc = runCatching { roomRef.get().await() }.getOrNull()
            val existingMessages = existingDoc?.toObject(ChatRoomDto::class.java)?.recentMessages
                ?: localChatRoomsFallback[roomId]?.recentMessages?.map {
                    ChatMessageDto(senderId = it.senderId, text = it.text, timestamp = it.timestamp)
                }
                ?: emptyList()
            val cappedMessages = (existingMessages + newMsg).takeLast(50)

            val participants = listOf(senderId, recipientId).distinct()
            val participantNames = mapOf(senderId to senderName, recipientId to recipientName)
            val participantPhotos = mapOf(senderId to senderPhoto, recipientId to recipientPhoto)

            val roomDto = ChatRoomDto(
                roomId = roomId,
                madrasaId = madrasaId,
                participants = participants,
                participantNames = participantNames,
                participantPhotos = participantPhotos,
                lastMessage = text.trim(),
                lastTimestamp = timestamp,
                recentMessages = cappedMessages,
            )
            localChatRoomsFallback[roomId] = roomDto.toDomain()

            val roomData = mapOf(
                "roomId" to roomId,
                "madrasaId" to madrasaId,
                "participants" to participants,
                "participantNames" to participantNames,
                "participantPhotos" to participantPhotos,
                "lastMessage" to text.trim(),
                "lastTimestamp" to timestamp,
                "recentMessages" to cappedMessages,
            )

            runCatching {
                roomRef.set(roomData, SetOptions.merge()).await()
            }

            // IMPORTANT: the actor's own device must NOT notify itself — a self-generated
            // local notification could never reach the other phone. Publish a
            // recipient-addressed notification instead, which the recipient's device
            // picks up from the `notifications` collection listener.
            runCatching {
                notificationHelper.publishChatNotification(
                    senderUid = senderId,
                    senderName = senderName,
                    recipientUid = recipientId,
                    messageText = text.trim(),
                    roomId = roomId,
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মেসেজ পাঠানো ব্যর্থ", e)
        }
    }
}
