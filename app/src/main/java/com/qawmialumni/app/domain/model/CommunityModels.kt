package com.qawmialumni.app.domain.model

data class Madrasa(
    val madrasaId: String = "",
    val code: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val address: String = "",
    val establishedYear: String = "",
    val adminUids: List<String> = emptyList(),
    val staffUids: List<String> = emptyList(),
    val totalAlumniCount: Int = 0,
    val createdAt: Long = 0L,
)

data class Ustad(
    val ustadId: String = "",
    val madrasaId: String = "",
    val name: String = "",
    val designation: String = "",
    val photoUrl: String = "",
    val specialization: String = "",
    val bio: String = "",
    val isActive: Boolean = true,
)

enum class ForumCategory(val wireValue: String, val banglaName: String) {
    ILMI_MUZAKARA("Ilmi_Muzakara", "علمي مذاكرة / ইলমি মুযাকারা"),
    MASAYEL("Masayel", "مسائل / শরয়ী মাসায়েল"),
    KHEDMAT_VACANCY("Khedmat_Vacancy", "খেদমত / চাকরির সুযোগ"),
    GENERAL("General", "সাধারণ আলোচনা");

    companion object {
        fun fromWire(value: String?): ForumCategory =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: GENERAL
    }
}

data class PostComment(
    val commentId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
)

data class ForumPost(
    val postId: String = "",
    val madrasaId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val authorRole: UserRole = UserRole.ALUMNI,
    val category: ForumCategory = ForumCategory.ILMI_MUZAKARA,
    val title: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = 0L,
)

data class Notice(
    val noticeId: String = "",
    val madrasaId: String = "",
    val title: String = "",
    val details: String = "",
    val imageUrl: String = "",
    val eventDate: String = "",
    val createdByUid: String = "",
    val createdAt: Long = 0L,
)

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
)

data class ChatRoom(
    val roomId: String = "",
    val madrasaId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val recentMessages: List<ChatMessage> = emptyList(),
)
