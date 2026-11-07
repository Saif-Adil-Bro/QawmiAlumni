package com.qawmialumni.app.core.di

import com.qawmialumni.app.data.repository.AdminRepositoryImpl
import com.qawmialumni.app.data.repository.AlumniRepositoryImpl
import com.qawmialumni.app.data.repository.AuthRepositoryImpl
import com.qawmialumni.app.data.repository.CommunityRepositoryImpl
import com.qawmialumni.app.domain.repository.AdminRepository
import com.qawmialumni.app.domain.repository.AlumniRepository
import com.qawmialumni.app.domain.repository.AuthRepository
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        impl: AdminRepositoryImpl,
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindAlumniRepository(
        impl: AlumniRepositoryImpl,
    ): AlumniRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl,
    ): CommunityRepository
}
