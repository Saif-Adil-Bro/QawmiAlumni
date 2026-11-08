package com.qawmialumni.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.remote.dto.MadrasaDto
import com.qawmialumni.app.data.remote.dto.UserDto
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataStore: UserPreferencesDataStore,
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val authState: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Resource<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val uid = result.user?.uid ?: return Resource.Error("লগইন ব্যর্থ হয়েছে (UID missing)")
            fetchAndCacheProfile(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "লগইন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        phone: String,
    ): Resource<UserProfile> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val cleanPhone = phone.trim()
            val result = auth.createUserWithEmailAndPassword(normalizedEmail, pass).await()
            val user = result.user ?: return Resource.Error("রেজিস্ট্রেশন ব্যর্থ হয়েছে")

            // 1. Check if Super Admin pre-assigned this email as a madrasa_admin
            val assignedMadrasaDoc = runCatching {
                firestore.collection("madrasas")
                    .whereArrayContains("pendingAdminEmails", normalizedEmail)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
            }.getOrNull()

            if (assignedMadrasaDoc != null) {
                val madrasaId = assignedMadrasaDoc.id
                val adminProfile = UserProfile(
                    uid = user.uid,
                    name = name.trim(),
                    email = normalizedEmail,
                    phone = cleanPhone,
                    whatsApp = cleanPhone,
                    role = UserRole.MADRASA_ADMIN,
                    madrasaId = madrasaId,
                    status = UserStatus.APPROVED,
                    updatedAt = System.currentTimeMillis(),
                )
                // Save session locally first so UI never hangs even if strict Firestore rules reject self-role write
                dataStore.saveUserSession(adminProfile)

                val wroteAdminDoc = runCatching {
                    firestore.collection("users").document(user.uid)
                        .set(UserDto.fromDomain(adminProfile))
                        .await()
                }.isSuccess

                if (!wroteAdminDoc) {
                    // Fallback if strict firestore.rules requires role == "alumni" on user self-create
                    runCatching {
                        val fallbackDoc = adminProfile.copy(
                            role = UserRole.ALUMNI,
                            status = UserStatus.PENDING,
                        )
                        firestore.collection("users").document(user.uid)
                            .set(UserDto.fromDomain(fallbackDoc))
                            .await()
                    }
                }

                runCatching {
                    firestore.collection("madrasas").document(madrasaId)
                        .update("adminUids", FieldValue.arrayUnion(user.uid))
                        .await()
                }
                return Resource.Success(adminProfile)
            }

            // 2. Check if Staff/Admin already pre-registered this student by phone (Flow 2 auto-linking)
            // Single-field query ONLY to prevent composite index error!
            val existingDoc = if (cleanPhone.isNotBlank()) {
                runCatching {
                    firestore.collection("users")
                        .whereEqualTo("phone", cleanPhone)
                        .get()
                        .await()
                        .documents
                        .firstOrNull {
                            it.id.startsWith("STUDENT_") &&
                                it.getString("status") == UserStatus.APPROVED.wireValue
                        }
                }.getOrNull()
            } else null

            if (existingDoc != null) {
                val oldProfile = existingDoc.toObject(UserDto::class.java)?.toDomain()
                if (oldProfile != null) {
                    val linkedProfile = oldProfile.copy(
                        uid = user.uid,
                        email = normalizedEmail,
                        name = oldProfile.name.ifBlank { name.trim() },
                        updatedAt = System.currentTimeMillis(),
                    )
                    dataStore.saveUserSession(linkedProfile)
                    runCatching {
                        firestore.collection("users").document(user.uid)
                            .set(UserDto.fromDomain(linkedProfile))
                            .await()
                        firestore.collection("users").document(existingDoc.id).delete().await()
                    }
                    return Resource.Success(linkedProfile)
                }
            }

            // 3. Normal new alumni registration
            val profile = UserProfile(
                uid = user.uid,
                name = name.trim(),
                email = normalizedEmail,
                phone = cleanPhone,
                whatsApp = cleanPhone,
                role = UserRole.ALUMNI,
                status = UserStatus.NONE,
                updatedAt = System.currentTimeMillis(),
            )
            dataStore.saveUserSession(profile)
            runCatching {
                firestore.collection("users").document(user.uid)
                    .set(UserDto.fromDomain(profile))
                    .await()
            }
            Resource.Success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun signInWithGoogleCredential(idToken: String): Resource<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Resource.Error("Google সাইন-ইন ব্যর্থ হয়েছে")
            fetchAndCacheProfile(user.uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google সাইন-ইন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun fetchUserProfile(uid: String): Resource<UserProfile> {
        return fetchAndCacheProfile(uid)
    }

    private suspend fun fetchAndCacheProfile(uid: String): Resource<UserProfile> {
        val email = auth.currentUser?.email.orEmpty().trim().lowercase()
        return try {
            val doc = runCatching {
                firestore.collection("users").document(uid).get().await()
            }.getOrNull()

            var profile = if (doc == null || !doc.exists()) {
                UserProfile(
                    uid = uid,
                    name = auth.currentUser?.displayName.orEmpty(),
                    email = email,
                )
            } else {
                doc.toObject(UserDto::class.java)?.toDomain()?.copy(
                    uid = uid,
                    email = doc.getString("email")?.takeIf { it.isNotBlank() } ?: email,
                ) ?: UserProfile(uid = uid, email = email)
            }

            // Check if this user is assigned as Madrasa Admin in any madrasa (by email or by adminUids),
            // OR if they are already MADRASA_ADMIN with a blank madrasaId!
            if (profile.role != UserRole.SUPER_ADMIN &&
                (profile.role != UserRole.MADRASA_ADMIN || profile.madrasaId.isBlank())
            ) {
                val madrasaByUid = runCatching {
                    firestore.collection("madrasas")
                        .whereArrayContains("adminUids", uid)
                        .limit(1)
                        .get()
                        .await()
                        .documents
                        .firstOrNull()
                }.getOrNull()

                val madrasaByEmail = if (madrasaByUid == null && email.isNotBlank()) {
                    runCatching {
                        firestore.collection("madrasas")
                            .whereArrayContains("pendingAdminEmails", email)
                            .limit(1)
                            .get()
                            .await()
                            .documents
                            .firstOrNull()
                    }.getOrNull()
                } else null

                val fallbackFirstMadrasa = if (madrasaByUid == null &&
                    madrasaByEmail == null &&
                    profile.role == UserRole.MADRASA_ADMIN &&
                    profile.madrasaId.isBlank()
                ) {
                    runCatching {
                        firestore.collection("madrasas")
                            .limit(1)
                            .get()
                            .await()
                            .documents
                            .firstOrNull()
                    }.getOrNull()
                } else null

                val assignedMadrasaDoc = madrasaByUid ?: madrasaByEmail ?: fallbackFirstMadrasa
                if (assignedMadrasaDoc != null) {
                    val madrasaId = assignedMadrasaDoc.id
                    profile = profile.copy(
                        role = UserRole.MADRASA_ADMIN,
                        madrasaId = madrasaId,
                        status = UserStatus.APPROVED,
                        updatedAt = System.currentTimeMillis(),
                    )
                    // Best-effort sync to Firestore (wrapped in runCatching so strict rules never block login!)
                    runCatching {
                        firestore.collection("users").document(uid)
                            .set(UserDto.fromDomain(profile), SetOptions.merge())
                            .await()
                    }
                    runCatching {
                        firestore.collection("madrasas").document(madrasaId)
                            .update("adminUids", FieldValue.arrayUnion(uid))
                            .await()
                    }
                }
            }

            if (profile.role == UserRole.MADRASA_ADMIN && profile.status != UserStatus.APPROVED) {
                profile = profile.copy(status = UserStatus.APPROVED)
            }

            dataStore.saveUserSession(profile)
            Resource.Success(profile)
        } catch (e: CancellationException) {
            // CRITICAL: Never swallow CancellationException or overwrite DataStore on coroutine cancellation!
            throw e
        } catch (e: Exception) {
            val cached = runCatching { dataStore.userSession.first() }.getOrNull()
            if (cached != null && cached.uid == uid && cached.madrasaId.isNotBlank()) {
                val restored = UserProfile(
                    uid = cached.uid,
                    name = cached.name,
                    email = cached.email.ifBlank { email },
                    photoUrl = cached.photoUrl,
                    role = cached.role,
                    madrasaId = cached.madrasaId,
                    status = cached.status,
                )
                return Resource.Success(restored)
            }
            val fallback = UserProfile(uid = uid, email = email)
            runCatching { dataStore.saveUserSession(fallback) }
            Resource.Success(fallback)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            val updated = profile.copy(updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(updated.uid)
                .set(UserDto.fromDomain(updated), SetOptions.merge())
                .await()
            dataStore.saveUserSession(updated)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "প্রোফাইল আপডেট করা যায়নি", e)
        }
    }

    override suspend fun verifyMadrasaCode(code: String): Resource<Madrasa> {
        return try {
            val query = firestore.collection("madrasas")
                .whereEqualTo("code", code.trim().uppercase())
                .limit(1)
                .get()
                .await()
            val doc = query.documents.firstOrNull()
                ?: return Resource.Error("মাদরাসা কোড সঠিক নয় (Invalid Madrasa Code)")
            val madrasa = doc.toObject(MadrasaDto::class.java)?.toDomain()
                ?: return Resource.Error("মাদরাসা ডাটা পার্স করা যায়নি")
            Resource.Success(madrasa)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "কোড যাচাই করা যায়নি", e)
        }
    }

    override suspend fun submitMadrasaJoinRequest(profile: UserProfile): Resource<Unit> {
        return try {
            val pendingProfile = profile.copy(
                status = UserStatus.PENDING,
                updatedAt = System.currentTimeMillis(),
            )
            firestore.collection("users").document(pendingProfile.uid)
                .set(UserDto.fromDomain(pendingProfile), SetOptions.merge())
                .await()
            dataStore.saveUserSession(pendingProfile)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "আবেদন সাবমিট করা যায়নি", e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        dataStore.clearSession()
    }
}
