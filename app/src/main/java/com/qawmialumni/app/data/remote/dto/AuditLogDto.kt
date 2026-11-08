package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.qawmialumni.app.domain.model.AuditActionType
import com.qawmialumni.app.domain.model.AuditLog
import com.qawmialumni.app.domain.model.UserRole

@IgnoreExtraProperties
data class AuditLogDto(
    var logId: String = "",
    var madrasaId: String = "",
    var actorUid: String = "",
    var actorName: String = "",
    var actorRole: String = "ALUMNI",
    var actionType: String = "NOTICE_POSTED",
    var targetTitle: String = "",
    var details: String = "",
    var timestamp: Long = 0L,
) {
    fun toDomain(): AuditLog = AuditLog(
        logId = logId,
        madrasaId = madrasaId,
        actorUid = actorUid,
        actorName = actorName,
        actorRole = UserRole.fromWire(actorRole),
        actionType = AuditActionType.fromWire(actionType),
        targetTitle = targetTitle,
        details = details,
        timestamp = timestamp,
    )

    companion object {
        fun fromDomain(domain: AuditLog): AuditLogDto = AuditLogDto(
            logId = domain.logId,
            madrasaId = domain.madrasaId,
            actorUid = domain.actorUid,
            actorName = domain.actorName,
            actorRole = domain.actorRole.wireValue,
            actionType = domain.actionType.name,
            targetTitle = domain.targetTitle,
            details = domain.details,
            timestamp = domain.timestamp,
        )
    }
}
