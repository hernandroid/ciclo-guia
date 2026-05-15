package com.cicloguia.app.feature.map.data.remote

import com.cicloguia.app.feature.map.domain.model.CyclewaysDatasetMetadata

interface CyclewaysRemoteDataSource {
    suspend fun getMetadata(): CyclewaysDatasetMetadata
    suspend fun getGeoJson(url: String): String
}