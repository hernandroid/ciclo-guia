package com.cicloguia.app.data.cycleways.local

import com.cicloguia.app.domain.cycleways.model.CyclewaysDatasetMetadata

interface CyclewaysMetadataLocalDataSource {
    suspend fun getMetadata(): LocalCyclewaysMetadata
    suspend fun saveMetadata(metadata: CyclewaysDatasetMetadata)
}