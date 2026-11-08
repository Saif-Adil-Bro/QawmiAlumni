package com.qawmialumni.app.domain.model

enum class UserRole(val wireValue: String, val banglaLabel: String) {
    SUPER_ADMIN("super_admin", "সুপার অ্যাডমিন"),
    MADRASA_ADMIN("madrasa_admin", "মাদরাসা অ্যাডমিন"),
    STAFF("staff", "উস্তাদ / জিম্মাদার"),
    ALUMNI("alumni", "ফারেগ / আলামনাই");

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

typealias VerificationStatus = UserStatus

enum class KhedmatCategory(val wireValue: String, val banglaName: String) {
    TEACHING("Teaching", "تدريس • দরস ও শিক্ষকতা"),
    IMAMAT("Imamat", "إمامة • ইমামতি ও খতিব"),
    FATWA("Fatwa", "إفتاء • ফতোয়া ও গবেষণা"),
    DAWAH("Dawah", "دعوة • দাওয়াত ও ওয়ায"),
    BUSINESS("Business", "تجارة • হালাল ব্যবসা"),
    HIGHER_STUDY("Higher_Study", "دراسات • উচ্চশিক্ষা (তাখাসসুস)"),
    OTHER("Other", "অন্যান্য খেদমত");

    companion object {
        fun fromWire(value: String?): KhedmatCategory =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: OTHER
    }
}

/**
 * Qawmi Madrasa graduation department (তাকমীল / তাখাসসুসাত / হিফজ).
 */
enum class GraduationDepartment(val wireValue: String, val banglaName: String) {
    DAWRA_HADITH("Dawra_Hadith", "দাওরায়ে হাদিস (তাকমীল)"),
    IFTA("Ifta", "ইফতা ও ফিকহ (তাখাসসুস)"),
    TAFSIR("Tafsir", "তাফসীর ও উলুমুল কুরআন"),
    ULOOMUL_HADITH("Uloomul_Hadith", "উলুমুল হাদিস"),
    ADAB("Adab", "আরবি সাহিত্য (আদব)"),
    QIRAT("Qirat", "ইলমুল কিরাত ও তাজবীদ"),
    HIFZ("Hifz", "হিফযুল কুরআন");

    companion object {
        fun fromWire(value: String?): GraduationDepartment =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: DAWRA_HADITH
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
    val department: GraduationDepartment = GraduationDepartment.DAWRA_HADITH,
    val dakhilaNumber: String = "",
    val batchYearHijri: String = "",
    val batchYearEng: String = "",
    val supervisedByUstadId: String = "",
    val supervisedByUstadName: String = "",
    val currentKhedmat: String = "",
    val khedmatCategory: KhedmatCategory = KhedmatCategory.TEACHING,
    val bloodGroup: String = "",
    val district: String = "",
    val bio: String = "",
    val updatedAt: Long = 0L,
) {
    val isApproved: Boolean get() = status == UserStatus.APPROVED
    val isPending: Boolean get() = status == UserStatus.PENDING
}
