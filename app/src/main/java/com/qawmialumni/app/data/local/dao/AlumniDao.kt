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
     * Smart filtering query (Blueprint §7):
     * Matches batch, ustad, blood group, district, and free-text search across name/father/khedmat.
     * Empty string parameter means "all / no filter applied".
     */
    @Query("""
        SELECT * FROM alumni 
        WHERE madrasaId = :madrasaId 
          AND status = 'approved'
          AND (:batch = '' OR batchYearHijri = :batch)
          AND (:ustadId = '' OR supervisedByUstadId = :ustadId)
          AND (:bloodGroup = '' OR bloodGroup = :bloodGroup)
          AND (:district = '' OR district = :district)
          AND (:query = '' OR (name LIKE '%' || :query || '%' OR fatherName LIKE '%' || :query || '%' OR currentKhedmat LIKE '%' || :query || '%'))
        ORDER BY name ASC
    """)
    fun filterAlumni(
        madrasaId: String,
        batch: String = "",
        ustadId: String = "",
        bloodGroup: String = "",
        district: String = "",
        query: String = "",
    ): Flow<List<AlumniEntity>>

    @Query("SELECT MAX(updatedAt) FROM alumni WHERE madrasaId = :madrasaId")
    suspend fun getLatestUpdatedAt(madrasaId: String): Long?

    @Query("SELECT DISTINCT batchYearHijri FROM alumni WHERE madrasaId = :madrasaId AND batchYearHijri != '' ORDER BY batchYearHijri DESC")
    fun getDistinctBatches(madrasaId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM alumni WHERE madrasaId = :madrasaId AND status = 'approved'")
    fun getApprovedCount(madrasaId: String): Flow<Int>

    @Query("DELETE FROM alumni WHERE madrasaId = :madrasaId")
    suspend fun clearMadrasaAlumni(madrasaId: String)
}
