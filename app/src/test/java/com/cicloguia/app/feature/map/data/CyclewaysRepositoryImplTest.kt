package com.cicloguia.app.feature.map.data

import com.cicloguia.app.feature.map.data.local.CyclewaysAssetDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.feature.map.domain.model.CyclewaysDatasetMetadata
import com.cicloguia.app.feature.map.domain.model.LocalCyclewaysMetadata
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclewaysRepositoryImplTest {

    @Test
    fun `getCachedGeoJson returns cached file without reading embedded asset`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CACHED_GEOJSON
        )
        val assetDataSource = FakeCyclewaysAssetDataSource(
            assetGeoJson = ASSET_GEOJSON
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            assetDataSource = assetDataSource
        )

        val result = repository.getCachedGeoJson()

        assertEquals(CACHED_GEOJSON, result)
        assertFalse(assetDataSource.wasRead)
    }

    @Test
    fun `getCachedGeoJson returns embedded asset when cached file does not exist`() = runBlocking {
        val assetDataSource = FakeCyclewaysAssetDataSource(
            assetGeoJson = ASSET_GEOJSON
        )
        val repository = repository(
            fileDataSource = FakeCyclewaysFileDataSource(cachedGeoJson = null),
            assetDataSource = assetDataSource
        )

        val result = repository.getCachedGeoJson()

        assertEquals(ASSET_GEOJSON, result)
        assertTrue(assetDataSource.wasRead)
    }

    @Test
    fun `sync saves downloaded dataset instead of embedded asset`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(cachedGeoJson = null)
        val remoteDataSource = FakeCyclewaysRemoteDataSource(
            metadata = REMOTE_METADATA,
            geoJson = REMOTE_GEOJSON
        )
        val repository = repository(
            remoteDataSource = remoteDataSource,
            fileDataSource = fileDataSource,
            assetDataSource = FakeCyclewaysAssetDataSource(assetGeoJson = ASSET_GEOJSON),
            metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
                metadata = LocalCyclewaysMetadata(
                    version = null,
                    checksum = null
                )
            )
        )

        val result = repository.sync()

        assertEquals(SyncCyclewaysResult.Updated, result)
        assertEquals(REMOTE_GEOJSON, fileDataSource.savedGeoJson)
    }

    @Test
    fun `sync does not overwrite cache when dataset is already updated`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CACHED_GEOJSON
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
                metadata = LocalCyclewaysMetadata(
                    version = REMOTE_METADATA.version,
                    checksum = REMOTE_METADATA.checksum
                )
            )
        )

        val result = repository.sync()

        assertEquals(SyncCyclewaysResult.AlreadyUpdated, result)
        assertEquals(null, fileDataSource.savedGeoJson)
    }

    private fun repository(
        remoteDataSource: CyclewaysRemoteDataSource = FakeCyclewaysRemoteDataSource(
            metadata = REMOTE_METADATA,
            geoJson = REMOTE_GEOJSON
        ),
        fileDataSource: FakeCyclewaysFileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = null
        ),
        assetDataSource: CyclewaysAssetDataSource = FakeCyclewaysAssetDataSource(
            assetGeoJson = ASSET_GEOJSON
        ),
        metadataLocalDataSource: CyclewaysMetadataLocalDataSource =
            FakeCyclewaysMetadataLocalDataSource(
                metadata = LocalCyclewaysMetadata(
                    version = null,
                    checksum = null
                )
            )
    ): CyclewaysRepositoryImpl {
        return CyclewaysRepositoryImpl(
            remoteDataSource = remoteDataSource,
            fileDataSource = fileDataSource,
            assetDataSource = assetDataSource,
            metadataLocalDataSource = metadataLocalDataSource
        )
    }

    private class FakeCyclewaysFileDataSource(
        private val cachedGeoJson: String?
    ) : CyclewaysFileDataSource {

        var savedGeoJson: String? = null
            private set

        override suspend fun readGeoJson(): String? {
            return cachedGeoJson
        }

        override suspend fun saveGeoJson(content: String) {
            savedGeoJson = content
        }
    }

    private class FakeCyclewaysAssetDataSource(
        private val assetGeoJson: String?
    ) : CyclewaysAssetDataSource {

        var wasRead: Boolean = false
            private set

        override suspend fun readGeoJson(): String? {
            wasRead = true
            return assetGeoJson
        }
    }

    private class FakeCyclewaysRemoteDataSource(
        private val metadata: CyclewaysDatasetMetadata,
        private val geoJson: String
    ) : CyclewaysRemoteDataSource {

        override suspend fun getMetadata(): CyclewaysDatasetMetadata {
            return metadata
        }

        override suspend fun getGeoJson(url: String): String {
            return geoJson
        }
    }

    private class FakeCyclewaysMetadataLocalDataSource(
        private val metadata: LocalCyclewaysMetadata
    ) : CyclewaysMetadataLocalDataSource {

        var savedMetadata: CyclewaysDatasetMetadata? = null
            private set

        override suspend fun getMetadata(): LocalCyclewaysMetadata {
            return metadata
        }

        override suspend fun saveMetadata(metadata: CyclewaysDatasetMetadata) {
            savedMetadata = metadata
        }
    }

    private companion object {
        const val CACHED_GEOJSON = """{"type":"FeatureCollection","features":[]}"""
        const val ASSET_GEOJSON = """{"type":"FeatureCollection","features":[{"id":"asset"}]}"""
        const val REMOTE_GEOJSON = """{"type":"FeatureCollection","features":[{"id":"remote"}]}"""

        val REMOTE_METADATA = CyclewaysDatasetMetadata(
            version = "2026-05-12T15:47:46Z",
            lastUpdated = "2026-05-12T15:47:46Z",
            geoJsonUrl = "https://ciclo-guia.web.app/public-data/cycleways/latest.geojson",
            checksum = "remote-checksum",
            featureCount = 1,
            routeCount = 1
        )
    }
}
