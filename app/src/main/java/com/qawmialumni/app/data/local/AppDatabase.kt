package com.qawmialumni.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.data.local.dao.NoticeDao
import com.qawmialumni.app.data.local.dao.UstadDao
import com.qawmialumni.app.data.local.entity.AlumniEntity
import com.qawmialumni.app.data.local.entity.NoticeEntity
import com.qawmialumni.app.data.local.entity.UstadEntity

@Database(
    entities = [
        AlumniEntity::class,
        UstadEntity::class,
        NoticeEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alumniDao(): AlumniDao
    abstract fun ustadDao(): UstadDao
    abstract fun noticeDao(): NoticeDao

    companion object {
        const val DATABASE_NAME = "qawmi_alumni.db"
    }
}
