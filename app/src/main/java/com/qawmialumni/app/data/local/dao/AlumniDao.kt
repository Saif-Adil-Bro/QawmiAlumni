package com.qawmialumni.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qawmialumni.app.data.local.entity.AlumniEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumniDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlumni(alumni: List<AlumniEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSingle(alumni: AlumniEntity)

    @Query("SELECT * FROM alumni WHERE madrasaId = :madrasaId AND status = 'approved' ORDER BY name ASC")
    fun getApprovedAlumni(madrasaId: String): Flow<List<AlumniEntity>>

    @Query("SELECT * FROM alumni WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): AlumniEntity?

    @Query("SELECT * FROM alumni WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<AlumniEntity?>

    /**
     * Complete Smart Filtering Query (Blueprint §7.2):
     * Supports Hijri batch, supervised Ustad, blood group, graduation department, khedmat category, district, and search query.
     */
    @Query("""
        SELECT * FROM alumni 
        WHERE madrasaId = :madrasaId 
          AND status = 'approved'
          AND (:batch = '' OR batchYearHijri = :batch)
          AND (:ustadName = '' OR supervisedByUstadName LIKE '%' || :ustadName || '%')
          AND (:bloodGroup = '' OR bloodGroup = :bloodGroup)
          AND (:department = '' OR department = :department)
          AND (:khedmatCategory = '' OR khedmatCategory = :khedmatCategory)
          AND (:district = '' OR district LIKE '%' || :district || '%')
          AND (:query = '' OR (name LIKE '%' || :query || '%' OR fatherName LIKE '%' || :query || '%' OR currentKhedmat LIKE '%' || :query || '%' OR dakhilaNumber LIKE '%' || :query || '%'))
        ORDER BY batchYearHijri DESC, name ASC
    """)
    fun filterAlumni(
        madrasaId: String,
        batch: String = "",
        ustadName: String = "",
        bloodGroup: String = "",
        department: String = "",
        khedmatCategory: String = "",
        district: String = "",
        query: String = "",
    ): Flow<List<AlumniEntity>>

    @Query("SELECT MAX(updatedAt) FROM alumni WHERE madrasaId = :madrasaId")
    suspend fun getLatestUpdatedAt(madrasaId: String): Long?

    @Query("SELECT DISTINCT batchYearHijri FROM alumni WHERE madrasaId = :madrasaId AND batchYearHijri != '' ORDER BY batchYearHijri DESC")
    fun getDistinctBatches(madrasaId: String): Flow<List<String>>

    @Query("SELECT DISTINCT supervisedByUstadName FROM alumni WHERE madrasaId = :madrasaId AND supervisedByUstadName != '' ORDER BY supervisedByUstadName ASC")
    fun getDistinctUstadNames(madrasaId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM alumni WHERE madrasaId = :madrasaId AND status = 'approved'")
    fun getApprovedCount(madrasaId: String): Flow<Int>

    @Query("DELETE FROM alumni WHERE madrasaId = :madrasaId")
    suspend fun clearMadrasaAlumni(madrasaId: String)
}
