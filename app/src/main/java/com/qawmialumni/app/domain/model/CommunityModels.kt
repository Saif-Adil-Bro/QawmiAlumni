package com.qawmialumni.app.domain.model

data class Madrasa(
    val madrasaId: String = "",
    val code: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val address: String = "",
    val establishedYear: String = "",
    val pendingAdminEmails: List<String> = emptyList(),
    val adminUids: List<String> = emptyList(),
    val staffUids: List<String> = emptyList(),
    val totalAlumniCount: Int = 0,
    val createdAt: Long = 0L,
)

/**
 * Honorific suffix for Ustads in Qawmi tradition:
 * - دامَت بَرَكاتُهُم (দা.বা.) for living scholars
 * - رَحِمَهُ الله (রহ.) for deceased scholars
 */
enum class UstadHonorific(val wireValue: String, val banglaSuffix: String, val arabicSuffix: String) {
    DAMAT_BARAKATUHUM("DB", "(দা.বা.)", "دامت بركاتهم"),
    RAHIMAHULLAH("RA", "(রহ.)", "رحمه الله");

    companion object {
        fun fromWire(value: String?): UstadHonorific =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: DAMAT_BARAKATUHUM
    }
}

data class Ustad(
    val ustadId: String = "",
    val madrasaId: String = "",
    val name: String = "",
    val honorific: UstadHonorific = UstadHonorific.DAMAT_BARAKATUHUM,
    val designation: String = "",
    val photoUrl: String = "",
    val specialization: String = "",
    val bio: String = "",
    val isActive: Boolean = true,
) {
    val displayNameWithHonorific: String
        get() = "$name ${honorific.banglaSuffix}"
}

enum class ForumCategory(val wireValue: String, val banglaName: String) {
    ILMI_MUZAKARA("Ilmi_Muzakara", "علمي مذاكرة • ইলমি মুযাকারা"),
    MASAYEL("Masayel", "مسائل • মাসআলা ও তাহকীক"),
    KHEDMAT_VACANCY("Khedmat_Vacancy", "خدمة • মুদাররিস/ইমাম আবশ্যক"),
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
    val contactPhone: String = "", // For Khedmat/Job vacancy posts
    val imageUrl: String = "",
    val likesCount: Int = 0,
    val likedByUids: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val isPinned: Boolean = false,
    val approvalStatus: VerificationStatus = VerificationStatus.APPROVED,
    val createdAt: Long = 0L,
) {
    val isGlobal: Boolean
        get() = madrasaId == GLOBAL_FORUM_ID

    companion object {
        const val GLOBAL_FORUM_ID = "GLOBAL"
    }
}

data class Notice(
    val noticeId: String = "",
    val madrasaId: String = "",
    val title: String = "",
    val details: String = "",
    val imageUrl: String = "",
    val eventDate: String = "",
    val rsvpUids: List<String> = emptyList(), // Dastarbandi / Reunion RSVP attendance list
    val createdByUid: String = "",
    val createdAt: Long = 0L,
)

/**
 * Delivery state of a single chat message (WhatsApp-style ticks).
 *
 * - [SENDING]   : handed to the repository, not yet acknowledged by Firestore
 * - [SENT]      : written to Firestore (one grey tick)
 * - [DELIVERED] : the recipient's device fetched the room containing it (two grey ticks)
 * - [READ]      : the recipient opened the room / marked it read (two blue ticks)
 */
enum class MessageStatus(val wireValue: String) {
    SENDING("sending"),
    SENT("sent"),
    DELIVERED("delivered"),
    READ("read");

    companion object {
        fun fromWire(value: String?): MessageStatus =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: SENT
    }
}

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT,
    /** Soft delete — kept only long enough to render "এই বার্তাটি মুছে ফেলা হয়েছে". */
    val isDeleted: Boolean = false,
) {
    val isDeletedPlaceholder: Boolean get() = isDeleted || text.isBlank()
}

data class ChatRoom(
    val roomId: String = "",
    val madrasaId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val recentMessages: List<ChatMessage> = emptyList(),
    /** uid -> millis at which that participant last opened this room. */
    val lastReadAt: Map<String, Long> = emptyMap(),
    /** uid -> millis at which that participant's device last synced this room. */
    val lastDeliveredAt: Map<String, Long> = emptyMap(),
) {
    /** When [uid] last read this room (0 if never). */
    fun lastReadAtOf(uid: String): Long = lastReadAt[uid] ?: 0L

    /** When [uid]'s device last received this room's messages (0 if never). */
    fun lastDeliveredAtOf(uid: String): Long = lastDeliveredAt[uid] ?: 0L

    /**
     * WhatsApp tick state of [message] as seen by the user who sent it.
     * READ wins over DELIVERED which wins over SENT.
     */
    fun tickStatusFor(message: ChatMessage, peerUid: String): MessageStatus {
        if (message.status == MessageStatus.SENDING) return MessageStatus.SENDING
        if (lastReadAtOf(peerUid) >= message.timestamp) return MessageStatus.READ
        if (lastDeliveredAtOf(peerUid) >= message.timestamp) return MessageStatus.DELIVERED
        return if (message.status == MessageStatus.READ) MessageStatus.READ else MessageStatus.SENT
    }

    companion object {
        /** Deterministic roomId so two users always share one unique conversation bucket. */
        fun buildRoomId(uid1: String, uid2: String): String =
            if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}
