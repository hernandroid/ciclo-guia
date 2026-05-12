package com.cicloguia.app.core.storage

interface CyclewaysFileDataSource {
    suspend fun readGeoJson(): String?
    suspend fun saveGeoJson(content: String)
}