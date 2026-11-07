package com.qawmialumni.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.qawmialumni.app.core.network.ImageUploadRepository
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
) : AlumniRepository {

    override fun getAlumni(
        madrasaId: String,
        batch: String,
        ustadId: String,
        bloodGroup: String,
        district: String,
        query: String,
    ): Flow<List<UserProfile>> {
        return alumniDao.filterAlumni(
            madrasaId = madrasaId,
            batch = batch,
            ustadId = ustadId,
            bloodGroup = bloodGroup,
            district = district,
            query = query,
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getBatches(madrasaId: String): Flow<List<String>> {
        return alumniDao.getDistinctBatches(madrasaId)
    }

    /**
     * Incremental sync strategy (Blueprint §5.D.1):
     * Queries Firestore only for documents where updatedAt > lastLocalSyncTime.
     * Keeps reads strictly within Spark Free Quota (50k/day)!
     */
    override suspend fun syncAlumniFromRemote(madrasaId: String): Resource<Int> {
        return try {
            val lastUpdated = alumniDao.getLatestUpdatedAt(madrasaId) ?: 0L
            val snapshot = firestore.collection("users")
                .whereEqualTo("madrasaId", madrasaId)
                .whereEqualTo("status", UserStatus.APPROVED.wireValue)
                .whereGreaterThan("updatedAt", lastUpdated)
                .get()
                .await()

            val remoteUsers = snapshot.documents.mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
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
     * Saves with status = 'approved'.
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

            val uid = student.uid.ifBlank { "STUDENT_${UUID.randomUUID()}" }
            val finalStudent = student.copy(
                uid = uid,
                photoUrl = photoUrl,
                role = UserRole.ALUMNI,
                status = UserStatus.APPROVED,
                updatedAt = System.currentTimeMillis(),
            )

            // Save to Firestore
            firestore.collection("users").document(uid)
                .set(UserDto.fromDomain(finalStudent))
                .await()

            // Increment madrasa alumni counter
            firestore.collection("madrasas").document(student.madrasaId)
                .update("totalAlumniCount", FieldValue.increment(1))
                .await()

            // Cache immediately to Room
            alumniDao.upsertSingle(finalStudent.toEntity())

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "ছাত্র প্রোফাইল সংরক্ষণ ব্যর্থ", e)
        }
    }
}
