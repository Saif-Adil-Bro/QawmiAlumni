package com.qawmialumni.app.domain.repository

import android.net.Uri
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    // Forum (Madrasa-specific or Global where madrasaId == "GLOBAL")
    fun getForumPosts(
        madrasaId: String,
        category: ForumCategory? = null,
        includePendingForUid: String = "",
    ): Flow<Resource<List<ForumPost>>>

    // Super Admin Global Forum Moderation & Management
    fun getAllGlobalPostsForAdmin(): Flow<Resource<List<ForumPost>>>

    suspend fun approveGlobalPost(postId: String): Resource<Unit>

    suspend fun updateForumPost(
        postId: String,
        title: String,
        content: String,
        category: ForumCategory,
        contactPhone: String,
    ): Resource<Unit>

    suspend fun createPost(
        post: ForumPost,
        imageUri: Uri?,
    ): Resource<Unit>

    suspend fun toggleLikePost(
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean,
    ): Resource<Unit>

    suspend fun deletePost(postId: String): Resource<Unit>

    suspend fun togglePinPost(postId: String, isPinned: Boolean): Resource<Unit>

    fun getComments(postId: String): Flow<Resource<List<PostComment>>>

    suspend fun addComment(postId: String, comment: PostComment): Resource<Unit>

    // Bucketed Chat (Blueprint §5.D.2)
    fun getChatRooms(userId: String): Flow<Resource<List<ChatRoom>>>

    fun observeChatRoom(roomId: String): Flow<Resource<ChatRoom>>

    suspend fun sendMessage(
        roomId: String,
        madrasaId: String,
        senderId: String,
        recipientId: String,
        senderName: String,
        recipientName: String,
        senderPhoto: String,
        recipientPhoto: String,
        text: String,
    ): Resource<Unit>
}
