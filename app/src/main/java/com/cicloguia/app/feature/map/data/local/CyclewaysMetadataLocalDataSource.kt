package com.cicloguia.app.feature.map.data.local

import com.cicloguia.app.feature.map.domain.model.CyclewaysDatasetMetadata
import com.cicloguia.app.feature.map.domain.model.LocalCyclewaysMetadata

interface CyclewaysMetadataLocalDataSource {
    suspend fun getMetadata(): LocalCyclewaysMetadata
    suspend fun saveMetadata(metadata: CyclewaysDatasetMetadata)
}