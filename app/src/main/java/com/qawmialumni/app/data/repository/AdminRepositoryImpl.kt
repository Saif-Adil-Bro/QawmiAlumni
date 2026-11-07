package com.qawmialumni.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.qawmialumni.app.core.network.ImageUploadRepository
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.local.dao.NoticeDao
import com.qawmialumni.app.data.local.dao.UstadDao
import com.qawmialumni.app.data.local.entity.toEntity
import com.qawmialumni.app.data.remote.dto.MadrasaDto
import com.qawmialumni.app.data.remote.dto.NoticeDto
import com.qawmialumni.app.data.remote.dto.UserDto
import com.qawmialumni.app.data.remote.dto.UstadDto
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.model.Ustad
import com.qawmialumni.app.domain.repository.AdminRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageUploadRepository: ImageUploadRepository,
    private val ustadDao: UstadDao,
    private val noticeDao: NoticeDao,
) : AdminRepository {

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
            // Check code uniqueness
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
            val madrasa = Madrasa(
                madrasaId = madrasaId,
                code = normalizedCode,
                name = name.trim(),
                logoUrl = logoUrl,
                address = address.trim(),
                establishedYear = establishedYear.trim(),
                totalAlumniCount = 0,
                createdAt = System.currentTimeMillis(),
            )

            firestore.collection("madrasas").document(madrasaId)
                .set(MadrasaDto.fromDomain(madrasa))
                .await()

            // If an admin email was provided, assign madrasa_admin role if user already exists
            if (adminEmail.isNotBlank()) {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", adminEmail.trim().lowercase())
                    .limit(1)
                    .get()
                    .await()
                userQuery.documents.firstOrNull()?.let { doc ->
                    firestore.collection("users").document(doc.id)
                        .update(
                            mapOf(
                                "role" to "madrasa_admin",
                                "madrasaId" to madrasaId,
                                "status" to "approved",
                            )
                        ).await()
                    firestore.collection("madrasas").document(madrasaId)
                        .update("adminUids", FieldValue.arrayUnion(doc.id))
                        .await()
                }
            }

            Resource.Success(madrasa)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "মাদরাসা তৈরি করা যায়নি", e)
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
            }
        awaitClose { listener.remove() }
    }

    override fun getPendingJoinRequests(madrasaId: String): Flow<Resource<List<UserProfile>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("users")
            .whereEqualTo("madrasaId", madrasaId)
            .whereEqualTo("status", UserStatus.PENDING.wireValue)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "পেন্ডিং তালিকা লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun approveJoinRequest(userId: String, madrasaId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "status" to UserStatus.APPROVED.wireValue,
                        "updatedAt" to System.currentTimeMillis(),
                    )
                ).await()

            // Increment alumni counter
            firestore.collection("madrasas").document(madrasaId)
                .update("totalAlumniCount", FieldValue.increment(1))
                .await()

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
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "প্রত্যাখ্যান ব্যর্থ হয়েছে", e)
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

            firestore.collection("ustads").document(ustadId)
                .set(UstadDto.fromDomain(finalUstad))
                .await()

            // Cache locally to Room
            ustadDao.upsertUstads(listOf(finalUstad.toEntity()))

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "উস্তাদ যুক্ত করা যায়নি", e)
        }
    }

    override fun getUstads(madrasaId: String): Flow<Resource<List<Ustad>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("ustads")
            .whereEqualTo("madrasaId", madrasaId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "উস্তাদ তালিকা লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(UstadDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))
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

            firestore.collection("notices").document(noticeId)
                .set(NoticeDto.fromDomain(finalNotice))
                .await()

            // Cache locally to Room
            noticeDao.upsertNotices(listOf(finalNotice.toEntity()))

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "নোটিশ প্রকাশ করা যায়নি", e)
        }
    }

    override fun getNotices(madrasaId: String): Flow<Resource<List<Notice>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("notices")
            .whereEqualTo("madrasaId", madrasaId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "নোটিশ তালিকা লোড ত্রুটি", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(NoticeDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }
}
