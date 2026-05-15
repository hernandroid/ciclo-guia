package com.cicloguia.app.feature.map.data.local

interface CyclewaysFileDataSource {
    suspend fun readGeoJson(): String?
    suspend fun saveGeoJson(content: String)
}