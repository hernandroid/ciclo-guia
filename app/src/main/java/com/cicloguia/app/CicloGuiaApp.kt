package com.cicloguia.app

import android.app.Application
import com.cicloguia.app.feature.map.data.MapLibreInitializer
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import javax.inject.Inject

@HiltAndroidApp
class CicloGuiaApp : Application() {

    @Inject
    lateinit var mapLibreInitializer: MapLibreInitializer

    override fun onCreate() {
        super.onCreate()
        mapLibreInitializer.initialize()
    }

}