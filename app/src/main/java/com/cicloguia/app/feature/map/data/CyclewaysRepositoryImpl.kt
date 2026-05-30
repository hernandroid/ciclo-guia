package com.cicloguia.app.feature.map.data

import com.cicloguia.app.feature.map.data.local.CyclewaysAssetDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.repository.CyclewaysRepository
import javax.inject.Inject

class CyclewaysRepositoryImpl @Inject constructor(
    private val remoteDataSource: CyclewaysRemoteDataSource,
    private val fileDataSource: CyclewaysFileDataSource,
    private val assetDataSource: CyclewaysAssetDataSource,
    private val metadataLocalDataSource: CyclewaysMetadataLocalDataSource
) : CyclewaysRepository {

    override suspend fun getCachedGeoJson(): String? {
        val cachedGeoJson = fileDataSource.readGeoJson()

        if (cachedGeoJson.isValidGeoJson()) {
            return cachedGeoJson
        }

        return assetDataSource.readGeoJson()
            ?.takeIf { geoJson -> CyclewaysGeoJsonValidator.isValid(geoJson) }
    }

    override suspend fun sync(): SyncCyclewaysResult {
        return runCatching {
            val remoteMetadata = remoteDataSource.getMetadata()
            val localMetadata = metadataLocalDataSource.getMetadata()
            val cachedGeoJson = fileDataSource.readGeoJson()

            if (
                localMetadata.checksum == remoteMetadata.checksum &&
                cachedGeoJson.isValidGeoJson()
            ) {
                return SyncCyclewaysResult.AlreadyUpdated
            }

            val geoJson = remoteDataSource.getGeoJson(remoteMetadata.geoJsonUrl)

            validateDownloadedGeoJson(
                geoJson = geoJson,
                expectedChecksum = remoteMetadata.checksum
            )

            fileDataSource.saveGeoJson(geoJson)
            metadataLocalDataSource.saveMetadata(remoteMetadata)

            SyncCyclewaysResult.Updated
        }.getOrElse { error ->
            SyncCyclewaysResult.Failed(error)
        }
    }

    private fun validateDownloadedGeoJson(
        geoJson: String,
        expectedChecksum: String
    ) {
        check(CyclewaysGeoJsonValidator.isValid(geoJson)) {
            "Downloaded cycleways GeoJSON is invalid"
        }

        val actualChecksum = Sha256Checksum.calculate(geoJson)

        check(actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
            "Downloaded cycleways GeoJSON checksum mismatch"
        }
    }

    private fun String?.isValidGeoJson(): Boolean {
        return this != null && CyclewaysGeoJsonValidator.isValid(this)
    }
}
