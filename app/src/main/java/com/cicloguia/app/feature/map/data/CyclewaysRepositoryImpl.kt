package com.cicloguia.app.feature.map.data

import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.repository.CyclewaysRepository
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