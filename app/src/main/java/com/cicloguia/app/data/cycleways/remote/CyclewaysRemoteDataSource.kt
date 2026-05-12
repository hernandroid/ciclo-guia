package com.cicloguia.app.data.cycleways.remote

import com.cicloguia.app.domain.cycleways.model.CyclewaysDatasetMetadata

interface CyclewaysRemoteDataSource {
    suspend fun getMetadata(): CyclewaysDatasetMetadata
    suspend fun getGeoJson(url: String): String
}