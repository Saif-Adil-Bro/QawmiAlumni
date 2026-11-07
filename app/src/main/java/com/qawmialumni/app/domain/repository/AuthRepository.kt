package com.qawmialumni.app.domain.repository

import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val authState: Flow<String?>

    suspend fun signInWithEmail(email: String, pass: String): Resource<UserProfile>
    suspend fun signUpWithEmail(email: String, pass: String, name: String, phone: String): Resource<UserProfile>
    suspend fun signInWithGoogleCredential(idToken: String): Resource<UserProfile>
    suspend fun fetchUserProfile(uid: String): Resource<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun verifyMadrasaCode(code: String): Resource<Madrasa>
    suspend fun submitMadrasaJoinRequest(profile: UserProfile): Resource<Unit>
    suspend fun signOut()
}
