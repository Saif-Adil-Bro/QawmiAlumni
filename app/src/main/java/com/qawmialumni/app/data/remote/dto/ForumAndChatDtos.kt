package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.MessageStatus
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.VerificationStatus

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
    var contactPhone: String = "",
    var imageUrl: String = "",
    var likesCount: Int = 0,
    var likedByUids: List<String> = emptyList(),
    var commentsCount: Int = 0,
    var isPinned: Boolean = false,
    var approvalStatus: String = "approved",
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
        contactPhone = contactPhone,
        imageUrl = imageUrl,
        likesCount = likesCount,
        likedByUids = likedByUids,
        commentsCount = commentsCount,
        isPinned = isPinned,
        approvalStatus = if (approvalStatus.isBlank()) {
            VerificationStatus.APPROVED
        } else {
            VerificationStatus.fromWire(approvalStatus)
        },
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
            contactPhone = post.contactPhone,
            imageUrl = post.imageUrl,
            likesCount = post.likesCount,
            likedByUids = post.likedByUids,
            commentsCount = post.commentsCount,
            isPinned = post.isPinned,
            approvalStatus = post.approvalStatus.wireValue,
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
    var messageId: String = "",
    var senderId: String = "",
    var text: String = "",
    var timestamp: Long = 0L,
    var status: String = MessageStatus.SENT.wireValue,
    var isDeleted: Boolean = false,
) {
    fun toDomain(): ChatMessage = ChatMessage(
        // Messages written before messageId was introduced have no id — derive a stable one
        // so LazyColumn keys stay unique and delete can find the right bubble.
        messageId = messageId.ifBlank { "LEGACY_${senderId}_$timestamp" },
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = MessageStatus.fromWire(status),
        isDeleted = isDeleted,
    )

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageDto = ChatMessageDto(
            messageId = msg.messageId,
            senderId = msg.senderId,
            text = msg.text,
            timestamp = msg.timestamp,
            status = msg.status.wireValue,
            isDeleted = msg.isDeleted,
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
    var lastReadAt: Map<String, Long> = emptyMap(),
    var lastDeliveredAt: Map<String, Long> = emptyMap(),
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
        lastReadAt = lastReadAt,
        lastDeliveredAt = lastDeliveredAt,
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
            lastReadAt = room.lastReadAt,
            lastDeliveredAt = room.lastDeliveredAt,
        )
    }
}
