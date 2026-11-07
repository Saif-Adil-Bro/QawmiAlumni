package com.qawmialumni.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus

@Entity(
    tableName = "alumni",
    indices = [
        Index(value = ["madrasaId"]),
        Index(value = ["madrasaId", "batchYearHijri"]),
        Index(value = ["madrasaId", "supervisedByUstadId"]),
        Index(value = ["madrasaId", "bloodGroup"]),
        Index(value = ["madrasaId", "department"]),
        Index(value = ["updatedAt"]),
    ]
)
data class AlumniEntity(
    @PrimaryKey val uid: String,
    val madrasaId: String,
    val name: String,
    val fatherName: String,
    val phone: String,
    val whatsApp: String,
    val email: String,
    val photoUrl: String,
    val role: String,
    val status: String,
    val department: String,
    val dakhilaNumber: String,
    val batchYearHijri: String,
    val batchYearEng: String,
    val supervisedByUstadId: String,
    val supervisedByUstadName: String,
    val currentKhedmat: String,
    val khedmatCategory: String,
    val bloodGroup: String,
    val district: String,
    val bio: String,
    val updatedAt: Long,
)

fun AlumniEntity.toDomain(): UserProfile = UserProfile(
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

fun UserProfile.toEntity(): AlumniEntity = AlumniEntity(
    uid = uid,
    madrasaId = madrasaId,
    name = name,
    fatherName = fatherName,
    phone = phone,
    whatsApp = whatsApp,
    email = email,
    photoUrl = photoUrl,
    role = role.wireValue,
    status = status.wireValue,
    department = department.wireValue,
    dakhilaNumber = dakhilaNumber,
    batchYearHijri = batchYearHijri,
    batchYearEng = batchYearEng,
    supervisedByUstadId = supervisedByUstadId,
    supervisedByUstadName = supervisedByUstadName,
    currentKhedmat = currentKhedmat,
    khedmatCategory = khedmatCategory.wireValue,
    bloodGroup = bloodGroup,
    district = district,
    bio = bio,
    updatedAt = updatedAt,
)
