package com.cicloguia.app.di

import com.cicloguia.app.BuildConfig
import com.cicloguia.app.core.map.MapStyleProvider
import com.cicloguia.app.core.map.MapTilerStyleProvider
import com.cicloguia.app.core.map.OpenFreeMapStyleProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    private const val PROVIDER_OPEN_FREE_MAP = "openfreemap"
    private const val PROVIDER_MAPTILER = "maptiler"

    @Provides
    @Singleton
    fun provideMapStyleProvider(): MapStyleProvider {
        return when (BuildConfig.MAP_PROVIDER.lowercase()) {
            PROVIDER_MAPTILER -> MapTilerStyleProvider(
                apiKey = BuildConfig.MAPTILER_API_KEY
            )

            PROVIDER_OPEN_FREE_MAP -> OpenFreeMapStyleProvider()

            else -> OpenFreeMapStyleProvider()
        }
    }
}