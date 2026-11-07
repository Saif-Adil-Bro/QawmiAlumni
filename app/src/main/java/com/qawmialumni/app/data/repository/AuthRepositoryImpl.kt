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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
            val pendingAdminMadrasaQuery = firestore.collection("madrasas")
                .whereArrayContains("pendingAdminEmails", normalizedEmail)
                .limit(1)
                .get()
                .await()

            val assignedMadrasaDoc = pendingAdminMadrasaQuery.documents.firstOrNull()
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
                firestore.collection("users").document(user.uid)
                    .set(UserDto.fromDomain(adminProfile))
                    .await()
                firestore.collection("madrasas").document(madrasaId)
                    .update(
                        mapOf(
                            "adminUids" to FieldValue.arrayUnion(user.uid),
                            "pendingAdminEmails" to FieldValue.arrayRemove(normalizedEmail),
                        )
                    ).await()
                dataStore.saveUserSession(adminProfile)
                return Resource.Success(adminProfile)
            }

            // 2. Check if Staff/Admin already pre-registered this student by phone or email (Flow 2 auto-linking!)
            val preRegQuery = if (cleanPhone.isNotBlank()) {
                firestore.collection("users")
                    .whereEqualTo("phone", cleanPhone)
                    .whereEqualTo("status", UserStatus.APPROVED.wireValue)
                    .limit(1)
                    .get()
                    .await()
            } else null

            val existingDoc = preRegQuery?.documents?.firstOrNull { it.id.startsWith("STUDENT_") }
            if (existingDoc != null) {
                val oldProfile = existingDoc.toObject(UserDto::class.java)?.toDomain()
                if (oldProfile != null) {
                    val linkedProfile = oldProfile.copy(
                        uid = user.uid,
                        email = normalizedEmail,
                        name = oldProfile.name.ifBlank { name.trim() },
                        updatedAt = System.currentTimeMillis(),
                    )
                    // Migrate to real Firebase Auth UID and remove placeholder document
                    firestore.collection("users").document(user.uid)
                        .set(UserDto.fromDomain(linkedProfile))
                        .await()
                    firestore.collection("users").document(existingDoc.id).delete().await()
                    dataStore.saveUserSession(linkedProfile)
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
            firestore.collection("users").document(user.uid)
                .set(UserDto.fromDomain(profile))
                .await()
            dataStore.saveUserSession(profile)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun signInWithGoogleCredential(idToken: String): Resource<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Resource.Error("Google সাইন-ইন ব্যর্থ হয়েছে")

            val doc = firestore.collection("users").document(user.uid).get().await()
            val profile = if (doc.exists()) {
                doc.toObject(UserDto::class.java)?.toDomain() ?: UserProfile(uid = user.uid)
            } else {
                val newProfile = UserProfile(
                    uid = user.uid,
                    name = user.displayName.orEmpty(),
                    email = user.email.orEmpty().lowercase(),
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    role = UserRole.ALUMNI,
                    status = UserStatus.NONE,
                    updatedAt = System.currentTimeMillis(),
                )
                firestore.collection("users").document(user.uid)
                    .set(UserDto.fromDomain(newProfile))
                    .await()
                newProfile
            }
            dataStore.saveUserSession(profile)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google সাইন-ইন ব্যর্থ হয়েছে", e)
        }
    }

    override suspend fun fetchUserProfile(uid: String): Resource<UserProfile> {
        return fetchAndCacheProfile(uid)
    }

    private suspend fun fetchAndCacheProfile(uid: String): Resource<UserProfile> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                val fallback = UserProfile(uid = uid, email = auth.currentUser?.email.orEmpty())
                return Resource.Success(fallback)
            }
            val profile = doc.toObject(UserDto::class.java)?.toDomain()
                ?: UserProfile(uid = uid)
            dataStore.saveUserSession(profile)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "প্রোফাইল লোড করা যায়নি", e)
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
