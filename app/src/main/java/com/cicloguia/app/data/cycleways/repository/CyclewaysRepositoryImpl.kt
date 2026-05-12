package com.cicloguia.app.data.cycleways.repository

import com.cicloguia.app.core.storage.CyclewaysFileDataSource
import com.cicloguia.app.data.cycleways.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.data.cycleways.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.domain.cycleways.model.SyncCyclewaysResult
import com.cicloguia.app.domain.cycleways.repository.CyclewaysRepository
import javax.inject.Inject

class CyclewaysRepositoryImpl @Inject constructor(
    private val remoteDataSource: CyclewaysRemoteDataSource,
    private val fileDataSource: CyclewaysFileDataSource,
    private val metadataLocalDataSource: CyclewaysMetadataLocalDataSource
) : CyclewaysRepository {

    override suspend fun getCachedGeoJson(): String? {
        return fileDataSource.readGeoJson()
    }

    override suspend fun sync(): SyncCyclewaysResult {
        return runCatching {
            val remoteMetadata = remoteDataSource.getMetadata()
            val localMetadata = metadataLocalDataSource.getMetadata()

            if (localMetadata.checksum == remoteMetadata.checksum) {
                return SyncCyclewaysResult.AlreadyUpdated
            }

            val geoJson = remoteDataSource.getGeoJson(remoteMetadata.geoJsonUrl)

            fileDataSource.saveGeoJson(geoJson)
            metadataLocalDataSource.saveMetadata(remoteMetadata)

            SyncCyclewaysResult.Updated
        }.getOrElse { error ->
            SyncCyclewaysResult.Failed(error)
        }
    }
}