package com.cicloguia.app.di

import com.cicloguia.app.core.network.HttpClient
import com.cicloguia.app.core.network.OkHttpClientImpl
import com.cicloguia.app.core.storage.CyclewaysFileDataSource
import com.cicloguia.app.data.cycleways.local.CyclewaysFileDataSourceImpl
import com.cicloguia.app.data.cycleways.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.data.cycleways.local.CyclewaysMetadataLocalDataSourceImpl
import com.cicloguia.app.data.cycleways.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.data.cycleways.remote.CyclewaysRemoteDataSourceImpl
import com.cicloguia.app.data.cycleways.repository.CyclewaysRepositoryImpl
import com.cicloguia.app.domain.cycleways.repository.CyclewaysRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CyclewaysDataModule {

    @Binds
    abstract fun bindRepository(
        impl: CyclewaysRepositoryImpl
    ): CyclewaysRepository

    @Binds
    abstract fun bindRemoteDataSource(
        impl: CyclewaysRemoteDataSourceImpl
    ): CyclewaysRemoteDataSource

    @Binds
    abstract fun bindFileDataSource(
        impl: CyclewaysFileDataSourceImpl
    ): CyclewaysFileDataSource

    @Binds
    abstract fun bindMetadataLocalDataSource(
        impl: CyclewaysMetadataLocalDataSourceImpl
    ): CyclewaysMetadataLocalDataSource

    @Binds
    abstract fun bindHttpClient(
        impl: OkHttpClientImpl
    ): HttpClient
}