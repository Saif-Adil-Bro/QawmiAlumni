package com.qawmialumni.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: return Resource.Error("রেজিস্ট্রেশন ব্যর্থ হয়েছে")
            val profile = UserProfile(
                uid = user.uid,
                name = name.trim(),
                email = user.email.orEmpty(),
                phone = phone.trim(),
                role = UserRole.ALUMNI,
                status = UserStatus.NONE,
                updatedAt = System.currentTimeMillis(),
            )
            // Persist initial user document to Firestore
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

            // Check if user document already exists
            val doc = firestore.collection("users").document(user.uid).get().await()
            val profile = if (doc.exists()) {
                doc.toObject(UserDto::class.java)?.toDomain() ?: UserProfile(uid = user.uid)
            } else {
                val newProfile = UserProfile(
                    uid = user.uid,
                    name = user.displayName.orEmpty(),
                    email = user.email.orEmpty(),
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
