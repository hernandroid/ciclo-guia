package com.cicloguia.app.di

import com.cicloguia.app.BuildConfig
import com.cicloguia.app.feature.map.data.CyclewaysRepositoryImpl
import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSourceImpl
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSourceImpl
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSourceImpl
import com.cicloguia.app.feature.map.data.style.MapStyleProvider
import com.cicloguia.app.feature.map.data.style.MapTilerStyleProvider
import com.cicloguia.app.feature.map.data.style.OpenFreeMapStyleProvider
import com.cicloguia.app.feature.map.domain.repository.CyclewaysRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    @Singleton
    abstract fun bindCyclewaysRepository(
        impl: CyclewaysRepositoryImpl
    ): CyclewaysRepository

    @Binds
    @Singleton
    abstract fun bindCyclewaysRemoteDataSource(
        impl: CyclewaysRemoteDataSourceImpl
    ): CyclewaysRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCyclewaysFileDataSource(
        impl: CyclewaysFileDataSourceImpl
    ): CyclewaysFileDataSource

    @Binds
    @Singleton
    abstract fun bindCyclewaysMetadataLocalDataSource(
        impl: CyclewaysMetadataLocalDataSourceImpl
    ): CyclewaysMetadataLocalDataSource

    companion object {

        private const val PROVIDER_OPEN_FREE_MAP = "openfreemap"
        private const val PROVIDER_MAPTILER = "maptiler"

        @Provides
        @Singleton
        fun provideMapStyleProvider(
            openFreeMapStyleProvider: OpenFreeMapStyleProvider
        ): MapStyleProvider {
            return when (BuildConfig.MAP_PROVIDER.lowercase()) {
                PROVIDER_MAPTILER -> MapTilerStyleProvider(
                    apiKey = BuildConfig.MAPTILER_API_KEY
                )

                PROVIDER_OPEN_FREE_MAP -> openFreeMapStyleProvider

                else -> openFreeMapStyleProvider
            }
        }
    }
}