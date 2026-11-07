package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.Ustad

@IgnoreExtraProperties
data class UstadDto(
    var ustadId: String = "",
    var madrasaId: String = "",
    var name: String = "",
    var designation: String = "",
    var photoUrl: String = "",
    var specialization: String = "",
    var bio: String = "",
    var isActive: Boolean = true,
) {
    fun toDomain(): Ustad = Ustad(
        ustadId = ustadId,
        madrasaId = madrasaId,
        name = name,
        designation = designation,
        photoUrl = photoUrl,
        specialization = specialization,
        bio = bio,
        isActive = isActive,
    )

    companion object {
        fun fromDomain(ustad: Ustad): UstadDto = UstadDto(
            ustadId = ustad.ustadId,
            madrasaId = ustad.madrasaId,
            name = ustad.name,
            designation = ustad.designation,
            photoUrl = ustad.photoUrl,
            specialization = ustad.specialization,
            bio = ustad.bio,
            isActive = ustad.isActive,
        )
    }
}

@IgnoreExtraProperties
data class NoticeDto(
    var noticeId: String = "",
    var madrasaId: String = "",
    var title: String = "",
    var details: String = "",
    var imageUrl: String = "",
    var eventDate: String = "",
    var createdByUid: String = "",
    var createdAt: Long = 0L,
) {
    fun toDomain(): Notice = Notice(
        noticeId = noticeId,
        madrasaId = madrasaId,
        title = title,
        details = details,
        imageUrl = imageUrl,
        eventDate = eventDate,
        createdByUid = createdByUid,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(notice: Notice): NoticeDto = NoticeDto(
            noticeId = notice.noticeId,
            madrasaId = notice.madrasaId,
            title = notice.title,
            details = notice.details,
            imageUrl = notice.imageUrl,
            eventDate = notice.eventDate,
            createdByUid = notice.createdByUid,
            createdAt = notice.createdAt,
        )
    }
}
