package com.cicloguia.app.di

import com.cicloguia.app.core.map.MapStyleProvider
import com.cicloguia.app.core.map.OpenFreeMapStyleProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    @Singleton
    abstract fun bindMapStyleProvider(
        implementation: OpenFreeMapStyleProvider
    ): MapStyleProvider
}