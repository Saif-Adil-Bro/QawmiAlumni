package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus

@IgnoreExtraProperties
data class UserDto(
    var uid: String = "",
    var name: String = "",
    var fatherName: String = "",
    var phone: String = "",
    var whatsApp: String = "",
    var email: String = "",
    var photoUrl: String = "",
    var role: String = "alumni",
    var madrasaId: String = "",
    var status: String = "none",
    var department: String = "Dawra_Hadith",
    var dakhilaNumber: String = "",
    var batchYearHijri: String = "",
    var batchYearEng: String = "",
    var supervisedByUstadId: String = "",
    var supervisedByUstadName: String = "",
    var currentKhedmat: String = "",
    var khedmatCategory: String = "Teaching",
    var bloodGroup: String = "",
    var district: String = "",
    var bio: String = "",
    var updatedAt: Long = 0L,
) {
    fun toDomain(): UserProfile = UserProfile(
        uid = uid,
        name = name,
        fatherName = fatherName,
        phone = phone,
        whatsApp = whatsApp,
        email = email,
        photoUrl = photoUrl,
        role = UserRole.fromWire(role),
        madrasaId = madrasaId,
        status = UserStatus.fromWire(status),
        department = GraduationDepartment.fromWire(department),
        dakhilaNumber = dakhilaNumber,
        batchYearHijri = batchYearHijri,
        batchYearEng = batchYearEng,
        supervisedByUstadId = supervisedByUstadId,
        supervisedByUstadName = supervisedByUstadName,
        currentKhedmat = currentKhedmat,
        khedmatCategory = KhedmatCategory.fromWire(khedmatCategory),
        bloodGroup = bloodGroup,
        district = district,
        bio = bio,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(user: UserProfile): UserDto = UserDto(
            uid = user.uid,
            name = user.name,
            fatherName = user.fatherName,
            phone = user.phone,
            whatsApp = user.whatsApp,
            email = user.email,
            photoUrl = user.photoUrl,
            role = user.role.wireValue,
            madrasaId = user.madrasaId,
            status = user.status.wireValue,
            department = user.department.wireValue,
            dakhilaNumber = user.dakhilaNumber,
            batchYearHijri = user.batchYearHijri,
            batchYearEng = user.batchYearEng,
            supervisedByUstadId = user.supervisedByUstadId,
            supervisedByUstadName = user.supervisedByUstadName,
            currentKhedmat = user.currentKhedmat,
            khedmatCategory = user.khedmatCategory.wireValue,
            bloodGroup = user.bloodGroup,
            district = user.district,
            bio = user.bio,
            updatedAt = user.updatedAt,
        )
    }
}

@IgnoreExtraProperties
data class MadrasaDto(
    var madrasaId: String = "",
    var code: String = "",
    var name: String = "",
    var logoUrl: String = "",
    var address: String = "",
    var establishedYear: String = "",
    var pendingAdminEmails: List<String> = emptyList(),
    var adminUids: List<String> = emptyList(),
    var staffUids: List<String> = emptyList(),
    var totalAlumniCount: Int = 0,
    var createdAt: Long = 0L,
) {
    fun toDomain(): Madrasa = Madrasa(
        madrasaId = madrasaId,
        code = code,
        name = name,
        logoUrl = logoUrl,
        address = address,
        establishedYear = establishedYear,
        pendingAdminEmails = pendingAdminEmails,
        adminUids = adminUids,
        staffUids = staffUids,
        totalAlumniCount = totalAlumniCount,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(madrasa: Madrasa): MadrasaDto = MadrasaDto(
            madrasaId = madrasa.madrasaId,
            code = madrasa.code,
            name = madrasa.name,
            logoUrl = madrasa.logoUrl,
            address = madrasa.address,
            establishedYear = madrasa.establishedYear,
            pendingAdminEmails = madrasa.pendingAdminEmails,
            adminUids = madrasa.adminUids,
            staffUids = madrasa.staffUids,
            totalAlumniCount = madrasa.totalAlumniCount,
            createdAt = madrasa.createdAt,
        )
    }
}
