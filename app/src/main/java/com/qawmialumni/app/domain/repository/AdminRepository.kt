package com.qawmialumni.app.domain.repository

import android.net.Uri
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.Ustad
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // Super Admin functions
    suspend fun createMadrasa(
        name: String,
        code: String,
        address: String,
        establishedYear: String,
        adminEmail: String,
        logoUri: Uri?,
    ): Resource<Madrasa>

    fun getAllMadrasas(): Flow<Resource<List<Madrasa>>>

    // Madrasa Admin & Staff functions
    fun getPendingJoinRequests(madrasaId: String): Flow<Resource<List<UserProfile>>>

    suspend fun approveJoinRequest(
        userId: String,
        madrasaId: String,
    ): Resource<Unit>

    suspend fun rejectJoinRequest(userId: String): Resource<Unit>

    /** Assign or revoke Staff / Batch Representative (জিম্মাদার) role for an approved alumni. */
    suspend fun updateMemberRole(
        userId: String,
        madrasaId: String,
        newRole: UserRole,
    ): Resource<Unit>

    suspend fun addUstad(
        ustad: Ustad,
        photoUri: Uri?,
    ): Resource<Unit>

    fun getUstads(madrasaId: String): Flow<Resource<List<Ustad>>>

    suspend fun postNotice(
        notice: Notice,
        imageUri: Uri?,
    ): Resource<Unit>

    fun getNotices(madrasaId: String): Flow<Resource<List<Notice>>>

    /** Dastarbandi / Alumni Reunion attendance RSVP toggle. */
    suspend fun toggleNoticeRsvp(
        noticeId: String,
        userId: String,
        isCurrentlyGoing: Boolean,
    ): Resource<Unit>
}
