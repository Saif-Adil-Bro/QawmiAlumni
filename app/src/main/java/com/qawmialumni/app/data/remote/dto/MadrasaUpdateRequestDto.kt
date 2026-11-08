package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.MadrasaUpdateRequest
import com.qawmialumni.app.domain.model.VerificationStatus

@IgnoreExtraProperties
data class MadrasaUpdateRequestDto(
    var requestId: String = "",
    var madrasaId: String = "",
    var madrasaCode: String = "",
    var currentName: String = "",
    var requestedName: String = "",
    var currentLogoUrl: String = "",
    var requestedLogoUrl: String = "",
    var currentAddress: String = "",
    var requestedAddress: String = "",
    var currentEstablishedYear: String = "",
    var requestedEstablishedYear: String = "",
    var requestedAdminEmail: String = "",
    var requestedByUid: String = "",
    var requestedByName: String = "",
    var status: String = "pending",
    var reviewNote: String = "",
    var createdAt: Long = 0L,
    var reviewedAt: Long = 0L,
) {
    fun toDomain(): MadrasaUpdateRequest = MadrasaUpdateRequest(
        requestId = requestId,
        madrasaId = madrasaId,
        madrasaCode = madrasaCode,
        currentName = currentName,
        requestedName = requestedName,
        currentLogoUrl = currentLogoUrl,
        requestedLogoUrl = requestedLogoUrl,
        currentAddress = currentAddress,
        requestedAddress = requestedAddress,
        currentEstablishedYear = currentEstablishedYear,
        requestedEstablishedYear = requestedEstablishedYear,
        requestedAdminEmail = requestedAdminEmail,
        requestedByUid = requestedByUid,
        requestedByName = requestedByName,
        status = VerificationStatus.fromWire(status),
        reviewNote = reviewNote,
        createdAt = createdAt,
        reviewedAt = reviewedAt,
    )

    companion object {
        fun fromDomain(domain: MadrasaUpdateRequest): MadrasaUpdateRequestDto = MadrasaUpdateRequestDto(
            requestId = domain.requestId,
            madrasaId = domain.madrasaId,
            madrasaCode = domain.madrasaCode,
            currentName = domain.currentName,
            requestedName = domain.requestedName,
            currentLogoUrl = domain.currentLogoUrl,
            requestedLogoUrl = domain.requestedLogoUrl,
            currentAddress = domain.currentAddress,
            requestedAddress = domain.requestedAddress,
            currentEstablishedYear = domain.currentEstablishedYear,
            requestedEstablishedYear = domain.requestedEstablishedYear,
            requestedAdminEmail = domain.requestedAdminEmail,
            requestedByUid = domain.requestedByUid,
            requestedByName = domain.requestedByName,
            status = domain.status.wireValue,
            reviewNote = domain.reviewNote,
            createdAt = domain.createdAt,
            reviewedAt = domain.reviewedAt,
        )
    }
}
