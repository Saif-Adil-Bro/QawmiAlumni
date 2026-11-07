package com.qawmialumni.app.data.local

import com.qawmialumni.app.data.local.entity.AlumniEntity
import com.qawmialumni.app.data.local.entity.toDomain
import com.qawmialumni.app.data.local.entity.toEntity
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.model.Ustad
import com.qawmialumni.app.domain.model.UstadHonorific
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelMappingTest {

    @Test
    fun `userProfile converts to entity and back without data loss`() {
        val original = UserProfile(
            uid = "uid_123",
            name = "মাওলানা আব্দুল্লাহ",
            fatherName = "হাফেজ ইব্রাহিম",
            phone = "+8801700000000",
            whatsApp = "+8801700000000",
            email = "abdullah@email.com",
            photoUrl = "https://i.ibb.co/test.webp",
            role = UserRole.ALUMNI,
            madrasaId = "MADRASA_101",
            status = UserStatus.APPROVED,
            dakhilaNumber = "1442-089",
            department = GraduationDepartment.IFTA,
            batchYearHijri = "1444",
            batchYearEng = "2023",
            supervisedByUstadId = "ustad_01",
            supervisedByUstadName = "আল্লামা আহমদ",
            currentKhedmat = "মুহাদ্দিস ও মুফতি",
            khedmatCategory = KhedmatCategory.FATWA,
            bloodGroup = "O+",
            district = "Dhaka",
            bio = "হিফজ, দাওরায়ে হাদীস ও ইফতা সম্পন্ন",
            updatedAt = 1710000050000L,
        )

        val entity: AlumniEntity = original.toEntity()
        val restored: UserProfile = entity.toDomain()

        assertEquals(original, restored)
        assertTrue(restored.isApproved)
        assertEquals(GraduationDepartment.IFTA, restored.department)
        assertEquals(KhedmatCategory.FATWA, restored.khedmatCategory)
    }

    @Test
    fun `role, status, department and honorific wire value fallbacks work gracefully`() {
        assertEquals(UserRole.ALUMNI, UserRole.fromWire("unknown_role"))
        assertEquals(UserRole.SUPER_ADMIN, UserRole.fromWire("super_admin"))
        assertEquals(UserRole.MADRASA_ADMIN, UserRole.fromWire("madrasa_admin"))
        assertEquals(UserRole.STAFF, UserRole.fromWire("staff"))

        assertEquals(UserStatus.NONE, UserStatus.fromWire(null))
        assertEquals(UserStatus.NONE, UserStatus.fromWire("invalid"))
        assertEquals(UserStatus.PENDING, UserStatus.fromWire("pending"))
        assertEquals(UserStatus.APPROVED, UserStatus.fromWire("approved"))
        assertEquals(UserStatus.REJECTED, UserStatus.fromWire("rejected"))

        assertEquals(GraduationDepartment.DAWRA_HADITH, GraduationDepartment.fromWire(null))
        assertEquals(GraduationDepartment.IFTA, GraduationDepartment.fromWire("IFTA"))
        assertEquals(GraduationDepartment.TAFSIR, GraduationDepartment.fromWire("TAFSIR"))

        val livingUstad = Ustad(name = "আল্লামা মাহমুদ", honorific = UstadHonorific.DAMAT_BARAKATUHUM)
        val deceasedUstad = Ustad(name = "আল্লামা কাসেম", honorific = UstadHonorific.RAHIMAHULLAH)
        assertEquals("আল্লামা মাহমুদ (দা.বা.)", livingUstad.displayNameWithHonorific)
        assertEquals("আল্লামা কাসেম (রহ.)", deceasedUstad.displayNameWithHonorific)
    }

    @Test
    fun `deterministic chat room id is symmetric regardless of sender order`() {
        val room1 = ChatRoom.buildRoomId("uid_alpha", "uid_beta")
        val room2 = ChatRoom.buildRoomId("uid_beta", "uid_alpha")
        assertEquals("uid_alpha_uid_beta", room1)
        assertEquals(room1, room2)
    }
}
