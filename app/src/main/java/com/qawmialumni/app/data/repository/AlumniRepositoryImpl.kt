package com.qawmialumni.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.network.ImageUploadRepository
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.data.local.entity.toDomain
import com.qawmialumni.app.data.local.entity.toEntity
import com.qawmialumni.app.data.remote.dto.UserDto
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.repository.AlumniRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlumniRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val alumniDao: AlumniDao,
    private val imageUploadRepository: ImageUploadRepository,
    private val dataStore: UserPreferencesDataStore,
    private val notificationHelper: NotificationHelper,
) : AlumniRepository {

    override fun getAlumni(
        madrasaId: String,
        batch: String,
        ustadName: String,
        bloodGroup: String,
        department: String,
        khedmatCategory: String,
        district: String,
        query: String,
    ): Flow<List<UserProfile>> {
        return alumniDao.filterAlumni(
            madrasaId = madrasaId,
            batch = batch,
            ustadName = ustadName,
            bloodGroup = bloodGroup,
            department = department,
            khedmatCategory = khedmatCategory,
            district = district,
            query = query,
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getBatches(madrasaId: String): Flow<List<String>> {
        return alumniDao.getDistinctBatches(madrasaId)
    }

    override fun getUstadNames(madrasaId: String): Flow<List<String>> {
        return alumniDao.getDistinctUstadNames(madrasaId)
    }

    /**
     * Single-field Firestore query (.whereEqualTo("madrasaId", madrasaId)) to completely avoid
     * FAILED_PRECONDITION composite index errors, while filtering approved alumni in memory
     * and syncing bi-directionally with Room.
     */
    override suspend fun syncAlumniFromRemote(madrasaId: String): Resource<Int> {
        if (madrasaId.isBlank()) return Resource.Success(0)
        return try {
            // 1. Best-effort push of any locally added STUDENT_* profiles to Firestore
            runCatching {
                val localApproved = alumniDao.getApprovedAlumni(madrasaId).first()
                localApproved.filter { it.uid.startsWith("STUDENT_") }.forEach { localEntity ->
                    firestore.collection("users").document(localEntity.uid)
                        .set(UserDto.fromDomain(localEntity.toDomain()), SetOptions.merge())
                        .await()
                }
            }

            // 2. Single-field query ONLY (no composite index required!)
            val snapshot = firestore.collection("users")
                .whereEqualTo("madrasaId", madrasaId)
                .get()
                .await()

            val remoteUsers = snapshot.documents
                .mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
                .filter { user ->
                    user.status == UserStatus.APPROVED &&
                        (user.role == UserRole.ALUMNI ||
                            user.role == UserRole.STAFF ||
                            user.uid.startsWith("STUDENT_") ||
                            user.batchYearHijri.isNotBlank())
                }

            if (remoteUsers.isNotEmpty()) {
                alumniDao.upsertAlumni(remoteUsers.map { it.toEntity() })
            }
            Resource.Success(remoteUsers.size)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "সিঙ্ক ব্যর্থ হয়েছে", e)
        }
    }

    /**
     * Flow 2: Direct Creation by Admin/Staff (Blueprint §5.B Flow 2)
     * Saves with status = 'approved' immediately to local Room DB and syncs to Firestore.
     */
    override suspend fun addStudentByStaff(student: UserProfile, photoUri: Uri?): Resource<Unit> {
        return try {
            var photoUrl = student.photoUrl
            if (photoUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(photoUri, ImageTarget.FULL)) {
                    is Resource.Success -> photoUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            var resolvedMadrasaId = student.madrasaId.trim()
            if (resolvedMadrasaId.isBlank()) {
                val cached = dataStore.userSession.first()
                resolvedMadrasaId = cached.madrasaId.trim()
                if (resolvedMadrasaId.isBlank()) {
                    resolvedMadrasaId = runCatching {
                        firestore.collection("madrasas").limit(1).get().await()
                            .documents.firstOrNull()?.id.orEmpty()
                    }.getOrDefault("")
                }
                if (resolvedMadrasaId.isNotBlank() && cached.uid.isNotBlank()) {
                    dataStore.saveUserSession(
                        UserProfile(
                            uid = cached.uid,
                            name = cached.name,
                            email = cached.email,
                            photoUrl = cached.photoUrl,
                            role = cached.role,
                            madrasaId = resolvedMadrasaId,
                            status = cached.status,
                        )
                    )
                }
            }

            if (resolvedMadrasaId.isBlank()) {
                return Resource.Error("মাদরাসা আইডি পাওয়া যায়নি। অনুগ্রহ করে পুনরায় লগইন করুন।")
            }

            val uid = student.uid.ifBlank { "STUDENT_${UUID.randomUUID()}" }
            val finalStudent = student.copy(
                uid = uid,
                madrasaId = resolvedMadrasaId,
                photoUrl = photoUrl,
                role = UserRole.ALUMNI,
                status = UserStatus.APPROVED,
                updatedAt = System.currentTimeMillis(),
            )

            // 1. Cache immediately to local Room DB first so it NEVER disappears locally!
            alumniDao.upsertSingle(finalStudent.toEntity())

            // 2. Save to Firestore (wrapped in runCatching so strict rules before update never lose local data)
            runCatching {
                firestore.collection("users").document(uid)
                    .set(UserDto.fromDomain(finalStudent))
                    .await()
            }

            // 3. Best-effort increment madrasa alumni counter
            runCatching {
                firestore.collection("madrasas").document(resolvedMadrasaId)
                    .update("totalAlumniCount", FieldValue.increment(1))
                    .await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "ছাত্র প্রোফাইল সংরক্ষণ ব্যর্থ", e)
        }
    }

    /**
     * Self-service profile update with optional <60KB WebP photo upload.
     */
    override suspend fun updateSelfProfile(
        profile: UserProfile,
        newPhotoUri: Uri?,
    ): Resource<UserProfile> {
        return try {
            var photoUrl = profile.photoUrl
            if (newPhotoUri != null) {
                when (val uploadRes = imageUploadRepository.uploadImage(newPhotoUri, ImageTarget.FULL)) {
                    is Resource.Success -> photoUrl = uploadRes.data
                    is Resource.Error -> return Resource.Error("ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                    Resource.Loading -> Unit
                }
            }

            val updatedProfile = profile.copy(
                photoUrl = photoUrl,
                updatedAt = System.currentTimeMillis(),
            )

            // Save locally first
            dataStore.saveUserSession(updatedProfile)
            if (updatedProfile.status == UserStatus.APPROVED) {
                alumniDao.upsertSingle(updatedProfile.toEntity())
            }

            // Save to Firestore (with fallback for strict role-lock rules)
            val savedFull = runCatching {
                firestore.collection("users").document(updatedProfile.uid)
                    .set(UserDto.fromDomain(updatedProfile), SetOptions.merge())
                    .await()
            }.isSuccess

            if (!savedFull) {
                runCatching {
                    firestore.collection("users").document(updatedProfile.uid)
                        .update(
                            mapOf(
                                "name" to updatedProfile.name,
                                "fatherName" to updatedProfile.fatherName,
                                "phone" to updatedProfile.phone,
                                "whatsApp" to updatedProfile.whatsApp,
                                "photoUrl" to updatedProfile.photoUrl,
                                "department" to updatedProfile.department.wireValue,
                                "batchYearHijri" to updatedProfile.batchYearHijri,
                                "supervisedByUstadName" to updatedProfile.supervisedByUstadName,
                                "currentKhedmat" to updatedProfile.currentKhedmat,
                                "khedmatCategory" to updatedProfile.khedmatCategory.wireValue,
                                "bloodGroup" to updatedProfile.bloodGroup,
                                "district" to updatedProfile.district,
                                "bio" to updatedProfile.bio,
                                "updatedAt" to updatedProfile.updatedAt,
                            )
                        ).await()
                }
            }

            // A profile change must alert the ADMINISTRATION (madrasa admin + super admin),
            // never the person who edited it.
            runCatching {
                notificationHelper.publishAdminNotification(
                    madrasaId = updatedProfile.madrasaId,
                    actorUid = updatedProfile.uid,
                    actorName = updatedProfile.name,
                    title = "প্রোফাইল হালনাগাদ হয়েছে",
                    message = "${updatedProfile.name.ifBlank { "একজন সদস্য" }} নিজের প্রোফাইল তথ্য পরিবর্তন করেছেন" +
                        (if (newPhotoUri != null) " (নতুন ছবি সহ)।" else "।"),
                    targetId = updatedProfile.uid,
                    includeSuperAdmins = true,
                )
            }

            Resource.Success(updatedProfile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "প্রোফাইল আপডেট ব্যর্থ", e)
        }
    }
}
