package com.qawmialumni.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.qawmialumni.app.core.network.ImageUploadRepository
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.data.local.dao.NoticeDao
import com.qawmialumni.app.data.local.dao.UstadDao
import com.qawmialumni.app.data.local.entity.toDomain
import com.qawmialumni.app.data.local.entity.toEntity
import com.qawmialumni.app.data.remote.dto.MadrasaDto
import com.qawmialumni.app.data.remote.dto.NoticeDto
import com.qawmialumni.app.data.remote.dto.UserDto
import com.qawmialumni.app.data.remote.dto.UstadDto
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.model.Ustad
import com.qawmialumni.app.domain.repository.AdminRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.data.remote.dto.AuditLogDto
import com.qawmialumni.app.data.remote.dto.MadrasaUpdateRequestDto
import com.qawmialumni.app.domain.model.AuditActionType
import com.qawmialumni.app.domain.model.AuditLog
import com.qawmialumni.app.domain.model.MadrasaUpdateRequest
import com.qawmialumni.app.domain.model.VerificationStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageUploadRepository: ImageUploadRepository,
    private val ustadDao: UstadDao,
    private val noticeDao: NoticeDao,
    private val alumniDao: AlumniDao,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val notificationHelper: NotificationHelper,
) : AdminRepository {

    private val localAuditLogsFallback = mutableMapOf<String, AuditLog>()
    private val localUpdateRequestsFallback = mutableMapOf<String, MadrasaUpdateRequest>()

    override suspend fun createMadrasa(
        name: String,
        code: String,
        address: String,
        establishedYear: String,
        adminEmail: String,
        logoUri: Uri?,
    ): Resource<Madrasa> {
        return try {
            val normalizedCode = code.trim().uppercase()
            val normalizedAdminEmail = adminEmail.trim().lowercase()
            val existing = firestore.collection("madrasas")
                .whereEqualTo("code", normalizedCode)
                .limit(1)
                .get()
                .await()
            if (!existing.isEmpty) {
                return Resource.Error("এই কোডটি ইতিমধ্যে ব্যবহৃত হচ্ছে। অন্য কোড দিন।")
            }

            var logoUrl = ""
            if (logoUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(logoUri, ImageTarget.THUMBNAIL)) {
                    is Resource.Success -> logoUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("লোগো আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val madrasaId = "MADRASA_${System.currentTimeMillis()}"
            var assignedAdminUid: String? = null
            if (normalizedAdminEmail.isNotBlank()) {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", normalizedAdminEmail)
                    .limit(1)
                    .get()
                    .await()
                assignedAdminUid = userQuery.documents.firstOrNull()?.id
            }

            val madrasa = Madrasa(
                madrasaId = madrasaId,
                code = normalizedCode,
                name = name.trim(),
                logoUrl = logoUrl,
                address = address.trim(),
                establishedYear = establishedYear.trim(),
                pendingAdminEmails = if (normalizedAdminEmail.isNotBlank()) {
                    listOf(normalizedAdminEmail)
                } else {
                    emptyList()
                },
                adminUids = listOfNotNull(assignedAdminUid),
                totalAlumniCount = 0,
                createdAt = System.currentTimeMillis(),
            )

            firestore.collection("madrasas").document(madrasaId)
                .set(MadrasaDto.fromDomain(madrasa))
                .await()

            if (assignedAdminUid != null) {
                runCatching {
                    firestore.collection("users").document(assignedAdminUid)
                        .update(
                            mapOf(
                                "role" to UserRole.MADRASA_ADMIN.wireValue,
                                "madrasaId" to madrasaId,
                                "status" to UserStatus.APPROVED.wireValue,
                                "updatedAt" to System.currentTimeMillis(),
                            )
                        ).await()
                }
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.MADRASA_CREATED,
                        targetTitle = name.trim(),
                        details = "নতুন মাদরাসা তৈরি হয়েছে (কোড: $normalizedCode)",
                    )
                )
            }

            Resource.Success(madrasa)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মাদরাসা তৈরি করা যায়নি", e)
        }
    }

    override suspend fun assignMadrasaAdminEmail(
        madrasaId: String,
        adminEmail: String,
    ): Resource<Unit> {
        return assignMultipleMadrasaAdminEmails(madrasaId, listOf(adminEmail))
    }

    override suspend fun assignMultipleMadrasaAdminEmails(
        madrasaId: String,
        adminEmails: List<String>,
    ): Resource<Unit> {
        return try {
            val normalizedEmails = adminEmails
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
                .distinct()

            if (normalizedEmails.isEmpty()) {
                return Resource.Error("সঠিক ইমেইল ঠিকানা প্রদান করুন (কমা দিয়ে একাধিক লিখতে পারেন)")
            }

            // 1. Add emails to madrasas/{madrasaId}.pendingAdminEmails
            firestore.collection("madrasas").document(madrasaId)
                .update("pendingAdminEmails", FieldValue.arrayUnion(*normalizedEmails.toTypedArray()))
                .await()

            // 2. For each email, if user document already exists, promote them directly as Madrasa Admin
            for (email in normalizedEmails) {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()
                val existingUserDoc = userQuery.documents.firstOrNull()
                if (existingUserDoc != null) {
                    val uid = existingUserDoc.id
                    firestore.collection("users").document(uid)
                        .update(
                            mapOf(
                                "role" to UserRole.MADRASA_ADMIN.wireValue,
                                "madrasaId" to madrasaId,
                                "status" to UserStatus.APPROVED.wireValue,
                                "updatedAt" to System.currentTimeMillis(),
                            )
                        ).await()
                    firestore.collection("madrasas").document(madrasaId)
                        .update("adminUids", FieldValue.arrayUnion(uid))
                        .await()
                }
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.ADMIN_ASSIGNED,
                        targetTitle = adminEmails.joinToString(", "),
                        details = "মাদরাসা অ্যাডমিন হিসেবে নতুন দায়িত্ব দেওয়া হয়েছে",
                    )
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অ্যাডমিন নিয়োগ ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun removeMadrasaAdmin(
        madrasaId: String,
        adminEmailOrUid: String,
        isUid: Boolean,
    ): Resource<Unit> {
        return try {
            val madrasaDoc = firestore.collection("madrasas").document(madrasaId)

            if (isUid) {
                // Remove from adminUids
                madrasaDoc.update("adminUids", FieldValue.arrayRemove(adminEmailOrUid)).await()
                // Demote user back to ALUMNI
                runCatching {
                    firestore.collection("users").document(adminEmailOrUid)
                        .update(
                            mapOf(
                                "role" to UserRole.ALUMNI.wireValue,
                                "updatedAt" to System.currentTimeMillis(),
                            )
                        ).await()
                }
            } else {
                // Remove from pendingAdminEmails
                val normalizedEmail = adminEmailOrUid.trim().lowercase()
                madrasaDoc.update("pendingAdminEmails", FieldValue.arrayRemove(normalizedEmail)).await()

                // Check if user account with this email exists and also demote them
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", normalizedEmail)
                    .limit(1)
                    .get()
                    .await()
                userQuery.documents.firstOrNull()?.id?.let { uid ->
                    madrasaDoc.update("adminUids", FieldValue.arrayRemove(uid)).await()
                    firestore.collection("users").document(uid)
                        .update(
                            mapOf(
                                "role" to UserRole.ALUMNI.wireValue,
                                "updatedAt" to System.currentTimeMillis(),
                            )
                        ).await()
                }
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.ADMIN_REMOVED,
                        targetTitle = adminEmailOrUid,
                        details = "মাদরাসা অ্যাডমিন পদবী থেকে অপসারণ করা হয়েছে",
                    )
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অ্যাডমিন অপসারণ করা যায়নি", e)
        }
    }

    override fun getAllMadrasas(): Flow<Resource<List<Madrasa>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("madrasas")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "ডাটা লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(MadrasaDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))

                // Automatically promote any pendingAdminEmails whose user accounts have now been created,
                // and push any locally cached students to Firestore using Super Admin privileges!
                launch {
                    list.forEach { madrasa ->
                        madrasa.pendingAdminEmails.forEach { pendingEmail ->
                            val cleanEmail = pendingEmail.trim().lowercase()
                            if (cleanEmail.isNotBlank()) {
                                runCatching {
                                    val userDocs = firestore.collection("users")
                                        .whereEqualTo("email", cleanEmail)
                                        .get()
                                        .await()
                                        .documents
                                    userDocs.forEach { uDoc ->
                                        val uid = uDoc.id
                                        val currentRole = uDoc.getString("role")
                                        val currentStatus = uDoc.getString("status")
                                        val currentMadrasa = uDoc.getString("madrasaId")
                                        if (currentRole != UserRole.MADRASA_ADMIN.wireValue ||
                                            currentStatus != UserStatus.APPROVED.wireValue ||
                                            currentMadrasa != madrasa.madrasaId
                                        ) {
                                            firestore.collection("users").document(uid)
                                                .update(
                                                    mapOf(
                                                        "role" to UserRole.MADRASA_ADMIN.wireValue,
                                                        "madrasaId" to madrasa.madrasaId,
                                                        "status" to UserStatus.APPROVED.wireValue,
                                                        "updatedAt" to System.currentTimeMillis(),
                                                    )
                                                ).await()
                                        }
                                        if (uid !in madrasa.adminUids) {
                                            firestore.collection("madrasas").document(madrasa.madrasaId)
                                                .update("adminUids", FieldValue.arrayUnion(uid))
                                                .await()
                                        }
                                    }
                                }
                            }
                        }

                        // Sync any locally cached students of this madrasa to Firestore
                        runCatching {
                            val localStudents = alumniDao.getApprovedAlumni(madrasa.madrasaId).first()
                            localStudents.forEach { entity ->
                                firestore.collection("users").document(entity.uid)
                                    .set(UserDto.fromDomain(entity.toDomain()))
                                    .await()
                            }
                        }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getPendingJoinRequests(madrasaId: String): Flow<Resource<List<UserProfile>>> = callbackFlow {
        trySend(Resource.Loading)
        // Single-field equality filter ONLY — prevents FAILED_PRECONDITION (composite index error)!
        val listener = firestore.collection("users")
            .whereEqualTo("madrasaId", madrasaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Success(emptyList()))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
                    ?.filter { it.status == UserStatus.PENDING }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun approveJoinRequest(userId: String, madrasaId: String): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "status" to UserStatus.APPROVED.wireValue,
                        "updatedAt" to now,
                    )
                ).await()

            runCatching {
                firestore.collection("madrasas").document(madrasaId)
                    .update("totalAlumniCount", FieldValue.increment(1))
                    .await()
            }

            // Immediately sync approved student into local Room DB
            val doc = firestore.collection("users").document(userId).get().await()
            val approvedUser = doc.toObject(UserDto::class.java)?.toDomain()
            approvedUser?.let {
                alumniDao.upsertSingle(it.toEntity())
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.STUDENT_APPROVED,
                        targetTitle = approvedUser?.name ?: "ইউজার ($userId)",
                        details = "ফারেগীন স্টুডেন্ট প্রোফাইল সফলভাবে অনুমোদন করা হয়েছে",
                    )
                )

                notificationHelper.publishDirectNotification(
                    recipientUid = userId,
                    actorUid = session.uid,
                    actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                    title = "✅ আপনার প্রোফাইল অনুমোদিত হয়েছে",
                    message = "আলহামদুলিল্লাহ, মাদরাসা কর্তৃপক্ষ আপনার প্রোফাইল অনুমোদন করেছেন। এখন অ্যাপের অ্যালবাম ও ফোরামে অংশ নিতে পারবেন।",
                    targetId = madrasaId,
                    type = com.qawmialumni.app.core.notification.NotificationType.ADMIN,
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অনুমোদন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun rejectJoinRequest(userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "status" to UserStatus.REJECTED.wireValue,
                        "updatedAt" to System.currentTimeMillis(),
                    )
                ).await()

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = session.madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.STUDENT_REJECTED,
                        targetTitle = "ইউজার ($userId)",
                        details = "ফারেগীন যোগদানের আবেদন বাতিল/বাতিল ঘোষণা করা হয়েছে",
                    )
                )

                notificationHelper.publishDirectNotification(
                    recipientUid = userId,
                    actorUid = session.uid,
                    actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                    title = "আপনার যোগদানের আবেদন অনুমোদিত হয়নি",
                    message = "মাদরাসা কর্তৃপক্ষ আপনার যোগদানের আবেদন বাতিল করেছেন। বিস্তারিত জানতে প্রশাসনের সাথে যোগাযোগ করুন।",
                    targetId = session.madrasaId,
                    type = com.qawmialumni.app.core.notification.NotificationType.ADMIN,
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "প্রত্যাখ্যান ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun updateMemberRole(
        userId: String,
        madrasaId: String,
        newRole: UserRole,
    ): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            alumniDao.getByUid(userId)?.let { existing ->
                alumniDao.upsertSingle(existing.copy(role = newRole.wireValue, updatedAt = now))
            }

            runCatching {
                firestore.collection("users").document(userId)
                    .update(
                        mapOf(
                            "role" to newRole.wireValue,
                            "updatedAt" to now,
                        )
                    ).await()
            }

            runCatching {
                val staffUpdate = if (newRole == UserRole.STAFF) {
                    FieldValue.arrayUnion(userId)
                } else {
                    FieldValue.arrayRemove(userId)
                }
                firestore.collection("madrasas").document(madrasaId)
                    .update("staffUids", staffUpdate)
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.ROLE_UPDATED,
                        targetTitle = "ইউজার ($userId)",
                        details = "সদস্যের পদবী পরিবর্তন করা হয়েছে (${newRole.wireValue})",
                    )
                )

                notificationHelper.publishDirectNotification(
                    recipientUid = userId,
                    actorUid = session.uid,
                    actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                    title = "আপনার পদবী পরিবর্তন করা হয়েছে",
                    message = "আপনার নতুন পদবী: ${newRole.banglaLabel}",
                    targetId = madrasaId,
                    type = com.qawmialumni.app.core.notification.NotificationType.ADMIN,
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "দায়িত্ব পরিবর্তন করা যায়নি", e)
        }
    }

    override suspend fun addUstad(ustad: Ustad, photoUri: Uri?): Resource<Unit> {
        return try {
            var photoUrl = ustad.photoUrl
            if (photoUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(photoUri, ImageTarget.THUMBNAIL)) {
                    is Resource.Success -> photoUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("উস্তাদের ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val ustadId = ustad.ustadId.ifBlank { "USTAD_${UUID.randomUUID()}" }
            val finalUstad = ustad.copy(ustadId = ustadId, photoUrl = photoUrl)

            // Cache immediately to Room
            ustadDao.upsertUstads(listOf(finalUstad.toEntity()))

            runCatching {
                firestore.collection("ustads").document(ustadId)
                    .set(UstadDto.fromDomain(finalUstad))
                    .await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "উস্তাদ যুক্ত করা যায়নি", e)
        }
    }

    override fun getUstads(madrasaId: String): Flow<Resource<List<Ustad>>> = callbackFlow {
        trySend(Resource.Loading)
        // Single-field equality filter ONLY — prevents FAILED_PRECONDITION (composite index error)!
        val listener = firestore.collection("ustads")
            .whereEqualTo("madrasaId", madrasaId)
            .addSnapshotListener { snapshot, error ->
                launch {
                    val localList = runCatching {
                        ustadDao.getActiveUstads(madrasaId).first().map { it.toDomain() }
                    }.getOrDefault(emptyList())

                    if (error != null) {
                        trySend(Resource.Success(localList))
                        return@launch
                    }

                    val remoteList = snapshot?.documents
                        ?.mapNotNull { it.toObject(UstadDto::class.java)?.toDomain() }
                        ?.filter { it.isActive }
                        .orEmpty()

                    if (remoteList.isNotEmpty()) {
                        runCatching { ustadDao.upsertUstads(remoteList.map { it.toEntity() }) }
                    }

                    val merged = (remoteList + localList).distinctBy { it.ustadId }
                    trySend(Resource.Success(merged))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun postNotice(notice: Notice, imageUri: Uri?): Resource<Unit> {
        return try {
            var imageUrl = notice.imageUrl
            if (imageUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(imageUri, ImageTarget.FULL)) {
                    is Resource.Success -> imageUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("নোটিশের ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val noticeId = notice.noticeId.ifBlank { "NOTICE_${System.currentTimeMillis()}" }
            val finalNotice = notice.copy(
                noticeId = noticeId,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis(),
            )

            // Cache immediately to Room
            noticeDao.upsertNotices(listOf(finalNotice.toEntity()))

            runCatching {
                firestore.collection("notices").document(noticeId)
                    .set(NoticeDto.fromDomain(finalNotice))
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = finalNotice.madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "মাদরাসা প্রশাসন" },
                        actorRole = session.role,
                        actionType = AuditActionType.NOTICE_POSTED,
                        targetTitle = finalNotice.title,
                        details = "নতুন নোটিশ বা দস্তারবন্দী সংক্রান্ত ঘোষণা জারি করা হয়েছে",
                    )
                )
                // Notify the madrasa community — the author is excluded inside the helper.
                notificationHelper.publishNoticeNotification(
                    madrasaId = finalNotice.madrasaId.ifBlank { session.madrasaId },
                    authorUid = session.uid,
                    authorName = session.name.ifBlank { "মাদরাসা প্রশাসন" },
                    title = finalNotice.title,
                    noticeId = noticeId,
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "নোটিশ প্রকাশ করা যায়নি", e)
        }
    }

    override fun getNotices(madrasaId: String): Flow<Resource<List<Notice>>> = callbackFlow {
        trySend(Resource.Loading)
        // Single-field equality filter ONLY — prevents FAILED_PRECONDITION (composite index error)!
        val listener = firestore.collection("notices")
            .whereEqualTo("madrasaId", madrasaId)
            .addSnapshotListener { snapshot, error ->
                launch {
                    val localList = runCatching {
                        noticeDao.getNotices(madrasaId).first().map { it.toDomain() }
                    }.getOrDefault(emptyList())

                    if (error != null) {
                        trySend(Resource.Success(localList.sortedByDescending { it.createdAt }))
                        return@launch
                    }

                    val remoteList = snapshot?.documents
                        ?.mapNotNull { it.toObject(NoticeDto::class.java)?.toDomain() }
                        .orEmpty()

                    if (remoteList.isNotEmpty()) {
                        runCatching { noticeDao.upsertNotices(remoteList.map { it.toEntity() }) }
                    }

                    val merged = (remoteList + localList)
                        .distinctBy { it.noticeId }
                        .sortedByDescending { it.createdAt }
                    trySend(Resource.Success(merged))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleNoticeRsvp(
        noticeId: String,
        userId: String,
        isCurrentlyGoing: Boolean,
    ): Resource<Unit> {
        return try {
            val updateOp = if (isCurrentlyGoing) {
                FieldValue.arrayRemove(userId)
            } else {
                FieldValue.arrayUnion(userId)
            }
            firestore.collection("notices").document(noticeId)
                .update("rsvpUids", updateOp)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অংশগ্রহণ নিশ্চিত করা যায়নি", e)
        }
    }

    override suspend fun recordAuditLog(log: AuditLog): Resource<Unit> {
        return try {
            val logId = log.logId.ifBlank { "LOG_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}" }
            val finalLog = log.copy(logId = logId, timestamp = if (log.timestamp > 0) log.timestamp else System.currentTimeMillis())
            localAuditLogsFallback[logId] = finalLog

            runCatching {
                firestore.collection("audit_logs").document(logId)
                    .set(AuditLogDto.fromDomain(finalLog))
                    .await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "লগ সংরক্ষণ করা যায়নি", e)
        }
    }

    override fun getAuditLogs(madrasaId: String): Flow<Resource<List<AuditLog>>> = callbackFlow {
        trySend(Resource.Loading)
        val query = if (madrasaId.isNotBlank() && madrasaId != "GLOBAL_ALL") {
            firestore.collection("audit_logs").whereEqualTo("madrasaId", madrasaId)
        } else {
            firestore.collection("audit_logs")
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val fallback = localAuditLogsFallback.values
                    .filter { madrasaId.isBlank() || madrasaId == "GLOBAL_ALL" || it.madrasaId == madrasaId }
                    .sortedByDescending { it.timestamp }
                trySend(Resource.Success(fallback))
                return@addSnapshotListener
            }
            val remoteList = snapshot?.documents
                ?.mapNotNull { it.toObject(AuditLogDto::class.java)?.toDomain() }
                .orEmpty()
            remoteList.forEach { localAuditLogsFallback[it.logId] = it }

            val merged = (remoteList + localAuditLogsFallback.values.filter {
                madrasaId.isBlank() || madrasaId == "GLOBAL_ALL" || it.madrasaId == madrasaId
            }).distinctBy { it.logId }
                .sortedByDescending { it.timestamp }
                .take(100)

            trySend(Resource.Success(merged))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun submitMadrasaUpdateRequest(
        request: MadrasaUpdateRequest,
        newLogoUri: Uri?,
    ): Resource<MadrasaUpdateRequest> {
        return try {
            val reqId = request.requestId.ifBlank { "MREQ_${System.currentTimeMillis()}" }
            var logoUrl = request.requestedLogoUrl
            if (newLogoUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(newLogoUri, ImageTarget.THUMBNAIL)) {
                    is Resource.Success -> logoUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("লোগো আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val finalRequest = request.copy(
                requestId = reqId,
                requestedLogoUrl = logoUrl,
                status = VerificationStatus.PENDING,
                createdAt = System.currentTimeMillis(),
            )

            localUpdateRequestsFallback[reqId] = finalRequest

            runCatching {
                firestore.collection("madrasa_update_requests").document(reqId)
                    .set(MadrasaUpdateRequestDto.fromDomain(finalRequest))
                    .await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = finalRequest.madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.MADRASA_UPDATE_REQUESTED,
                        targetTitle = finalRequest.requestedName.ifBlank { finalRequest.currentName },
                        details = "মাদরাসার তথ্য পরিবর্তনের আবেদন পাঠানো হয়েছে (কোড: ${finalRequest.madrasaCode})",
                    )
                )
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                // The request goes to the SUPER ADMIN, not to the requester's own device.
                notificationHelper.publishAdminNotification(
                    madrasaId = finalRequest.madrasaId,
                    actorUid = session.uid,
                    actorName = session.name.ifBlank { "মাদরাসা অ্যাডমিন" },
                    title = "নতুন মাদরাসা তথ্য সংশোধনের আবেদন",
                    message = "${finalRequest.requestedByName.ifBlank { session.name }} — " +
                        "${finalRequest.requestedName.ifBlank { finalRequest.currentName }} (কোড: ${finalRequest.madrasaCode})",
                    targetId = reqId,
                    includeSuperAdmins = true,
                )
            }

            Resource.Success(finalRequest)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "আবেদন পাঠানো যায়নি", e)
        }
    }

    override fun getPendingMadrasaUpdateRequests(madrasaId: String): Flow<Resource<List<MadrasaUpdateRequest>>> = callbackFlow {
        trySend(Resource.Loading)
        val query = if (madrasaId.isNotBlank()) {
            firestore.collection("madrasa_update_requests")
                .whereEqualTo("madrasaId", madrasaId)
        } else {
            firestore.collection("madrasa_update_requests")
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val fallback = localUpdateRequestsFallback.values
                    .filter { (madrasaId.isBlank() || it.madrasaId == madrasaId) && it.status == VerificationStatus.PENDING }
                    .sortedByDescending { it.createdAt }
                trySend(Resource.Success(fallback))
                return@addSnapshotListener
            }
            val remoteList = snapshot?.documents
                ?.mapNotNull { it.toObject(MadrasaUpdateRequestDto::class.java)?.toDomain() }
                .orEmpty()
            remoteList.forEach { localUpdateRequestsFallback[it.requestId] = it }

            val merged = (remoteList + localUpdateRequestsFallback.values.filter {
                (madrasaId.isBlank() || it.madrasaId == madrasaId)
            }).distinctBy { it.requestId }
                .filter { it.status == VerificationStatus.PENDING }
                .sortedByDescending { it.createdAt }

            trySend(Resource.Success(merged))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun approveMadrasaUpdateRequest(requestId: String): Resource<Unit> {
        return try {
            val reqDoc = firestore.collection("madrasa_update_requests").document(requestId).get().await()
            val request = reqDoc.toObject(MadrasaUpdateRequestDto::class.java)?.toDomain()
                ?: localUpdateRequestsFallback[requestId]
                ?: return Resource.Error("আবেদন খুঁজে পাওয়া যায়নি")

            val targetMadrasaId = request.madrasaId
            if (targetMadrasaId.isBlank()) return Resource.Error("মাদরাসা আইডি অবৈধ")

            // 1. Update the live Madrasa doc in Firestore
            val updates = mutableMapOf<String, Any>()
            if (request.requestedName.isNotBlank()) updates["name"] = request.requestedName.trim()
            if (request.requestedLogoUrl.isNotBlank()) updates["logoUrl"] = request.requestedLogoUrl.trim()
            if (request.requestedAddress.isNotBlank()) updates["address"] = request.requestedAddress.trim()
            if (request.requestedEstablishedYear.isNotBlank()) updates["establishedYear"] = request.requestedEstablishedYear.trim()

            if (updates.isNotEmpty()) {
                firestore.collection("madrasas").document(targetMadrasaId)
                    .update(updates)
                    .await()
            }

            // 2. If new admin email was provided, assign it
            if (request.requestedAdminEmail.isNotBlank()) {
                assignMultipleMadrasaAdminEmails(targetMadrasaId, listOf(request.requestedAdminEmail))
            }

            // 3. Update request status to APPROVED
            val updatedReq = request.copy(
                status = VerificationStatus.APPROVED,
                reviewedAt = System.currentTimeMillis(),
            )
            localUpdateRequestsFallback[requestId] = updatedReq

            runCatching {
                firestore.collection("madrasa_update_requests").document(requestId)
                    .update(
                        mapOf(
                            "status" to VerificationStatus.APPROVED.wireValue,
                            "reviewedAt" to System.currentTimeMillis(),
                        )
                    ).await()
            }

            // 4. Record audit log
            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = targetMadrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.MADRASA_UPDATE_APPROVED,
                        targetTitle = request.requestedName.ifBlank { request.currentName },
                        details = "সুপার অ্যাডমিন কর্তৃক মাদরাসার তথ্য পরিবর্তনের আবেদন অনুমোদিত হয়েছে",
                    )
                )
            }

            runCatching {
                val superSession = userPreferencesDataStore.userSession.first()
                val recipient = request.requestedByUid.ifBlank {
                    firestore.collection("users")
                        .whereEqualTo("madrasaId", targetMadrasaId)
                        .get().await().documents
                        .firstOrNull { it.getString("role") == UserRole.MADRASA_ADMIN.wireValue }
                        ?.id.orEmpty()
                }
                if (recipient.isNotBlank()) {
                    notificationHelper.publishDirectNotification(
                        recipientUid = recipient,
                        actorUid = superSession.uid,
                        actorName = superSession.name.ifBlank { "সুপার অ্যাডমিন" },
                        title = "✅ মাদরাসার তথ্য সংশোধনের আবেদন অনুমোদিত হয়েছে",
                        message = "${request.requestedName.ifBlank { request.currentName }} — আপনার আবেদন অনুমোদিত হয়েছে।",
                        targetId = targetMadrasaId,
                        type = com.qawmialumni.app.core.notification.NotificationType.ADMIN,
                    )
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "অনুমোদন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun rejectMadrasaUpdateRequest(requestId: String, note: String): Resource<Unit> {
        return try {
            val reqDoc = firestore.collection("madrasa_update_requests").document(requestId).get().await()
            val request = reqDoc.toObject(MadrasaUpdateRequestDto::class.java)?.toDomain()
                ?: localUpdateRequestsFallback[requestId]
                ?: return Resource.Error("আবেদন খুঁজে পাওয়া যায়নি")

            val updatedReq = request.copy(
                status = VerificationStatus.REJECTED,
                reviewNote = note,
                reviewedAt = System.currentTimeMillis(),
            )
            localUpdateRequestsFallback[requestId] = updatedReq

            runCatching {
                firestore.collection("madrasa_update_requests").document(requestId)
                    .update(
                        mapOf(
                            "status" to VerificationStatus.REJECTED.wireValue,
                            "reviewNote" to note,
                            "reviewedAt" to System.currentTimeMillis(),
                        )
                    ).await()
            }

            runCatching {
                val session = userPreferencesDataStore.userSession.first()
                recordAuditLog(
                    AuditLog(
                        madrasaId = request.madrasaId,
                        actorUid = session.uid,
                        actorName = session.name.ifBlank { "সুপার অ্যাডমিন" },
                        actorRole = session.role,
                        actionType = AuditActionType.MADRASA_UPDATE_REJECTED,
                        targetTitle = request.currentName,
                        details = "মাদরাসার তথ্য পরিবর্তনের আবেদন বাতিল করা হয়েছে${if (note.isNotBlank()) " (কারণ: $note)" else ""}",
                    )
                )
            }

            runCatching {
                val superSession = userPreferencesDataStore.userSession.first()
                val recipient = request.requestedByUid.ifBlank {
                    firestore.collection("users")
                        .whereEqualTo("madrasaId", request.madrasaId)
                        .get().await().documents
                        .firstOrNull { it.getString("role") == UserRole.MADRASA_ADMIN.wireValue }
                        ?.id.orEmpty()
                }
                if (recipient.isNotBlank()) {
                    notificationHelper.publishDirectNotification(
                        recipientUid = recipient,
                        actorUid = superSession.uid,
                        actorName = superSession.name.ifBlank { "সুপার অ্যাডমিন" },
                        title = "মাদরাসার তথ্য সংশোধনের আবেদন বাতিল হয়েছে",
                        message = "আপনার আবেদনটি অনুমোদিত হয়নি${if (note.isNotBlank()) " (কারণ: $note)" else ""}।",
                        targetId = request.madrasaId,
                        type = com.qawmialumni.app.core.notification.NotificationType.ADMIN,
                    )
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "আবেদন বাতিল ব্যর্থ হয়েছে", e)
        }
    }
}
