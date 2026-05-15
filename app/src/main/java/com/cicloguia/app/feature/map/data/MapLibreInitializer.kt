package com.cicloguia.app.feature.map.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import javax.inject.Inject

class MapLibreInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun initialize() {
        MapLibre.getInstance(
            context,
            "",
            WellKnownTileServer.MapLibre
        )
    }
}