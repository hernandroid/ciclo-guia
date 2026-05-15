package com.cicloguia.app.feature.map.domain.repository

import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult

interface CyclewaysRepository {
    suspend fun getCachedGeoJson(): String?
    suspend fun sync(): SyncCyclewaysResult
}