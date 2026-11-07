package com.qawmialumni.app.core.di

import android.content.Context
import androidx.room.Room
import com.qawmialumni.app.data.local.AppDatabase
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.data.local.dao.NoticeDao
import com.qawmialumni.app.data.local.dao.UstadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideAlumniDao(database: AppDatabase): AlumniDao = database.alumniDao()

    @Provides
    fun provideUstadDao(database: AppDatabase): UstadDao = database.ustadDao()

    @Provides
    fun provideNoticeDao(database: AppDatabase): NoticeDao = database.noticeDao()
}
