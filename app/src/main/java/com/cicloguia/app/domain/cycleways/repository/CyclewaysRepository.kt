package com.cicloguia.app.domain.cycleways.repository

import com.cicloguia.app.domain.cycleways.model.SyncCyclewaysResult

interface CyclewaysRepository {
    suspend fun getCachedGeoJson(): String?
    suspend fun sync(): SyncCyclewaysResult
}