package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.model.UserRole

@IgnoreExtraProperties
data class ForumPostDto(
    var postId: String = "",
    var madrasaId: String = "",
    var authorUid: String = "",
    var authorName: String = "",
    var authorPhoto: String = "",
    var authorRole: String = "alumni",
    var category: String = "Ilmi_Muzakara",
    var title: String = "",
    var content: String = "",
    var imageUrl: String = "",
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    var isPinned: Boolean = false,
    var createdAt: Long = 0L,
) {
    fun toDomain(): ForumPost = ForumPost(
        postId = postId,
        madrasaId = madrasaId,
        authorUid = authorUid,
        authorName = authorName,
        authorPhoto = authorPhoto,
        authorRole = UserRole.fromWire(authorRole),
        category = ForumCategory.fromWire(category),
        title = title,
        content = content,
        imageUrl = imageUrl,
        likesCount = likesCount,
        commentsCount = commentsCount,
        isPinned = isPinned,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(post: ForumPost): ForumPostDto = ForumPostDto(
            postId = post.postId,
            madrasaId = post.madrasaId,
            authorUid = post.authorUid,
            authorName = post.authorName,
            authorPhoto = post.authorPhoto,
            authorRole = post.authorRole.wireValue,
            category = post.category.wireValue,
            title = post.title,
            content = post.content,
            imageUrl = post.imageUrl,
            likesCount = post.likesCount,
            commentsCount = post.commentsCount,
            isPinned = post.isPinned,
            createdAt = post.createdAt,
        )
    }
}

@IgnoreExtraProperties
data class PostCommentDto(
    var commentId: String = "",
    var authorUid: String = "",
    var authorName: String = "",
    var authorPhoto: String = "",
    var text: String = "",
    var createdAt: Long = 0L,
) {
    fun toDomain(): PostComment = PostComment(
        commentId = commentId,
        authorUid = authorUid,
        authorName = authorName,
        authorPhoto = authorPhoto,
        text = text,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(comment: PostComment): PostCommentDto = PostCommentDto(
            commentId = comment.commentId,
            authorUid = comment.authorUid,
            authorName = comment.authorName,
            authorPhoto = comment.authorPhoto,
            text = comment.text,
            createdAt = comment.createdAt,
        )
    }
}

@IgnoreExtraProperties
data class ChatMessageDto(
    var senderId: String = "",
    var text: String = "",
    var timestamp: Long = 0L,
) {
    fun toDomain(): ChatMessage = ChatMessage(
        senderId = senderId,
        text = text,
        timestamp = timestamp,
    )

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageDto = ChatMessageDto(
            senderId = msg.senderId,
            text = msg.text,
            timestamp = msg.timestamp,
        )
    }
}

@IgnoreExtraProperties
data class ChatRoomDto(
    var roomId: String = "",
    var madrasaId: String = "",
    var participants: List<String> = emptyList(),
    var participantNames: Map<String, String> = emptyMap(),
    var participantPhotos: Map<String, String> = emptyMap(),
    var lastMessage: String = "",
    var lastTimestamp: Long = 0L,
    var recentMessages: List<ChatMessageDto> = emptyList(),
) {
    fun toDomain(): ChatRoom = ChatRoom(
        roomId = roomId,
        madrasaId = madrasaId,
        participants = participants,
        participantNames = participantNames,
        participantPhotos = participantPhotos,
        lastMessage = lastMessage,
        lastTimestamp = lastTimestamp,
        recentMessages = recentMessages.map { it.toDomain() },
    )

    companion object {
        fun fromDomain(room: ChatRoom): ChatRoomDto = ChatRoomDto(
            roomId = room.roomId,
            madrasaId = room.madrasaId,
            participants = room.participants,
            participantNames = room.participantNames,
            participantPhotos = room.participantPhotos,
            lastMessage = room.lastMessage,
            lastTimestamp = room.lastTimestamp,
            recentMessages = room.recentMessages.map { ChatMessageDto.fromDomain(it) },
        )
    }
}
