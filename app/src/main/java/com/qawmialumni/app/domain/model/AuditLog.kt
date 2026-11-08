package com.qawmialumni.app.domain.model

enum class AuditActionType(val banglaLabel: String) {
    STUDENT_APPROVED("স্টুডেন্ট অনুমোদন"),
    STUDENT_REJECTED("আবেদন বাতিল"),
    ADMIN_ASSIGNED("অ্যাডমিন নিয়োগ"),
    ADMIN_REMOVED("অ্যাডমিন অপসারণ"),
    ROLE_UPDATED("পদবী পরিবর্তন"),
    NOTICE_POSTED("নতুন নোটিশ জারি"),
    NOTICE_RSVP("নোটিশে অংশগ্রহণ"),
    FORUM_POST_CREATED("ফোরাম পোস্ট প্রকাশ"),
    FORUM_POST_APPROVED("ফোরাম পোস্ট অনুমোদন"),
    FORUM_POST_DELETED("ফোরাম পোস্ট মুছে ফেলা"),
    FORUM_POST_PINNED("ফোরাম পোস্ট পিন/আনপিন"),
    MADRASA_CREATED("নতুন মাদরাসা তৈরি"),
    USER_REGISTERED("নতুন নিবন্ধন");

    companion object {
        fun fromWire(value: String?): AuditActionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: NOTICE_POSTED
        }
    }
}

data class AuditLog(
    val logId: String = "",
    val madrasaId: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val actorRole: UserRole = UserRole.ALUMNI,
    val actionType: AuditActionType = AuditActionType.NOTICE_POSTED,
    val targetTitle: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
