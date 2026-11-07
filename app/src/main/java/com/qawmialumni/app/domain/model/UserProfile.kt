package com.qawmialumni.app.domain.model

enum class UserRole(val wireValue: String) {
    SUPER_ADMIN("super_admin"),
    MADRASA_ADMIN("madrasa_admin"),
    STAFF("staff"),
    ALUMNI("alumni");

    companion object {
        fun fromWire(value: String?): UserRole = when (value?.lowercase()) {
            "super_admin" -> SUPER_ADMIN
            "madrasa_admin" -> MADRASA_ADMIN
            "staff" -> STAFF
            else -> ALUMNI
        }
    }
}

enum class UserStatus(val wireValue: String) {
    NONE("none"),
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromWire(value: String?): UserStatus = when (value?.lowercase()) {
            "pending" -> PENDING
            "approved" -> APPROVED
            "rejected" -> REJECTED
            else -> NONE
        }
    }
}

enum class KhedmatCategory(val wireValue: String, val banglaName: String) {
    TEACHING("Teaching", "تدريس / শিক্ষকতা"),
    IMAMAT("Imamat", "إمامة / ইমামতি ও খতিব"),
    BUSINESS("Business", "تجارة / ব্যবসা"),
    HIGHER_STUDY("Higher_Study", "دراسات عليا / উচ্চশিক্ষা"),
    OTHER("Other", "অন্যান্য");

    companion object {
        fun fromWire(value: String?): KhedmatCategory =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: OTHER
    }
}

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val fatherName: String = "",
    val phone: String = "",
    val whatsApp: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.ALUMNI,
    val madrasaId: String = "",
    val status: UserStatus = UserStatus.NONE,
    val dakhilaNumber: String = "",
    val batchYearHijri: String = "",
    val batchYearEng: String = "",
    val supervisedByUstadId: String = "",
    val supervisedByUstadName: String = "",
    val currentKhedmat: String = "",
    val khedmatCategory: KhedmatCategory = KhedmatCategory.OTHER,
    val bloodGroup: String = "",
    val district: String = "",
    val bio: String = "",
    val updatedAt: Long = 0L,
) {
    val isApproved: Boolean get() = status == UserStatus.APPROVED
    val isPending: Boolean get() = status == UserStatus.PENDING
}
