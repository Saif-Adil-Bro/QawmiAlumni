package com.qawmialumni.app.domain.model

data class MadrasaUpdateRequest(
    val requestId: String = "",
    val madrasaId: String = "",
    val madrasaCode: String = "",
    val currentName: String = "",
    val requestedName: String = "",
    val currentLogoUrl: String = "",
    val requestedLogoUrl: String = "",
    val currentAddress: String = "",
    val requestedAddress: String = "",
    val currentEstablishedYear: String = "",
    val requestedEstablishedYear: String = "",
    val requestedAdminEmail: String = "",
    val requestedByUid: String = "",
    val requestedByName: String = "",
    val status: VerificationStatus = VerificationStatus.PENDING,
    val reviewNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L,
)
