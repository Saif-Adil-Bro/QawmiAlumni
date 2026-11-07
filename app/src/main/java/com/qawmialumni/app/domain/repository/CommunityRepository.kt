package com.qawmialumni.app.domain.repository

import android.net.Uri
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    // Forum
    fun getForumPosts(
        madrasaId: String,
        category: ForumCategory? = null,
    ): Flow<Resource<List<ForumPost>>>

    suspend fun createPost(
        post: ForumPost,
        imageUri: Uri?,
    ): Resource<Unit>

    suspend fun toggleLikePost(postId: String, currentLikes: Int): Resource<Unit>

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
