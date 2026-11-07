package com.qawmialumni.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.Ustad

@Entity(
    tableName = "ustads",
    indices = [Index(value = ["madrasaId"])]
)
data class UstadEntity(
    @PrimaryKey val ustadId: String,
    val madrasaId: String,
    val name: String,
    val designation: String,
    val photoUrl: String,
    val specialization: String,
    val bio: String,
    val isActive: Boolean,
)

fun UstadEntity.toDomain(): Ustad = Ustad(
    ustadId = ustadId,
    madrasaId = madrasaId,
    name = name,
    designation = designation,
    photoUrl = photoUrl,
    specialization = specialization,
    bio = bio,
    isActive = isActive,
)

fun Ustad.toEntity(): UstadEntity = UstadEntity(
    ustadId = ustadId,
    madrasaId = madrasaId,
    name = name,
    designation = designation,
    photoUrl = photoUrl,
    specialization = specialization,
    bio = bio,
    isActive = isActive,
)

@Entity(
    tableName = "notices",
    indices = [
        Index(value = ["madrasaId"]),
        Index(value = ["createdAt"]),
    ]
)
data class NoticeEntity(
    @PrimaryKey val noticeId: String,
    val madrasaId: String,
    val title: String,
    val details: String,
    val imageUrl: String,
    val eventDate: String,
    val createdByUid: String,
    val createdAt: Long,
)

fun NoticeEntity.toDomain(): Notice = Notice(
    noticeId = noticeId,
    madrasaId = madrasaId,
    title = title,
    details = details,
    imageUrl = imageUrl,
    eventDate = eventDate,
    createdByUid = createdByUid,
    createdAt = createdAt,
)

fun Notice.toEntity(): NoticeEntity = NoticeEntity(
    noticeId = noticeId,
    madrasaId = madrasaId,
    title = title,
    details = details,
    imageUrl = imageUrl,
    eventDate = eventDate,
    createdByUid = createdByUid,
    createdAt = createdAt,
)
