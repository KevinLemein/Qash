package com.kevinlemein.qash.di

import com.kevinlemein.qash.data.repository.SmsRepositoryImpl
import com.kevinlemein.qash.domain.repository.SmsRepository
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
    abstract fun bindSmsRepository(
        impl: SmsRepositoryImpl
    ): SmsRepository
}