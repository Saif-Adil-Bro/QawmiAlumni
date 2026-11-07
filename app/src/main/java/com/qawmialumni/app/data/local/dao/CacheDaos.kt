package com.qawmialumni.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qawmialumni.app.data.local.entity.NoticeEntity
import com.qawmialumni.app.data.local.entity.UstadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UstadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUstads(ustads: List<UstadEntity>)

    @Query("SELECT * FROM ustads WHERE madrasaId = :madrasaId AND isActive = 1 ORDER BY name ASC")
    fun getActiveUstads(madrasaId: String): Flow<List<UstadEntity>>

    @Query("DELETE FROM ustads WHERE madrasaId = :madrasaId")
    suspend fun clearByMadrasa(madrasaId: String)
}

@Dao
interface NoticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotices(notices: List<NoticeEntity>)

    @Query("SELECT * FROM notices WHERE madrasaId = :madrasaId ORDER BY createdAt DESC")
    fun getNotices(madrasaId: String): Flow<List<NoticeEntity>>

    @Query("DELETE FROM notices WHERE madrasaId = :madrasaId")
    suspend fun clearByMadrasa(madrasaId: String)
}
