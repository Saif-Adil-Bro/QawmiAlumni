package com.qawmialumni.app.domain.repository

import android.net.Uri
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AlumniRepository {
    fun getAlumni(
        madrasaId: String,
        batch: String = "",
        ustadName: String = "",
        bloodGroup: String = "",
        department: String = "",
        khedmatCategory: String = "",
        district: String = "",
        query: String = "",
    ): Flow<List<UserProfile>>

    fun getBatches(madrasaId: String): Flow<List<String>>

    fun getUstadNames(madrasaId: String): Flow<List<String>>

    suspend fun syncAlumniFromRemote(madrasaId: String): Resource<Int>

    suspend fun addStudentByStaff(
        student: UserProfile,
        photoUri: Uri?,
    ): Resource<Unit>

    suspend fun updateSelfProfile(
        profile: UserProfile,
        newPhotoUri: Uri?,
    ): Resource<UserProfile>
}
