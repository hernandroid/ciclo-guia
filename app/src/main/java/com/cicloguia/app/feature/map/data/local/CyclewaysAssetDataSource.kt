package com.cicloguia.app.feature.map.data.local

interface CyclewaysAssetDataSource {
    suspend fun readGeoJson(): String?
}
