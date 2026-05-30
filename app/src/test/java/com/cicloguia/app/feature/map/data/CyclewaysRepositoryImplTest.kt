package com.cicloguia.app.feature.map.data

import com.cicloguia.app.feature.map.data.local.CyclewaysAssetDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysFileDataSource
import com.cicloguia.app.feature.map.data.local.CyclewaysMetadataLocalDataSource
import com.cicloguia.app.feature.map.data.remote.CyclewaysRemoteDataSource
import com.cicloguia.app.feature.map.domain.model.CyclewaysGeoJsonSource
import com.cicloguia.app.feature.map.domain.model.CyclewaysDatasetMetadata
import com.cicloguia.app.feature.map.domain.model.LocalCyclewaysMetadata
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CyclewaysRepositoryImplTest {

    @Test
    fun `getCachedGeoJson returns valid cached file without reading embedded asset`() = runBlocking {
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

        assertEquals(CACHED_GEOJSON, result?.content)
        assertEquals(CyclewaysGeoJsonSource.DownloadedCache, result?.source)
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

        assertEquals(ASSET_GEOJSON, result?.content)
        assertEquals(CyclewaysGeoJsonSource.EmbeddedAsset, result?.source)
        assertTrue(assetDataSource.wasRead)
    }

    @Test
    fun `getCachedGeoJson returns embedded asset when cached file is corrupted`() = runBlocking {
        val repository = repository(
            fileDataSource = FakeCyclewaysFileDataSource(cachedGeoJson = CORRUPTED_GEOJSON),
            assetDataSource = FakeCyclewaysAssetDataSource(assetGeoJson = ASSET_GEOJSON)
        )

        val result = repository.getCachedGeoJson()

        assertEquals(ASSET_GEOJSON, result?.content)
        assertEquals(CyclewaysGeoJsonSource.EmbeddedAsset, result?.source)
    }

    @Test
    fun `getCachedGeoJson returns null when cache and asset are invalid`() = runBlocking {
        val repository = repository(
            fileDataSource = FakeCyclewaysFileDataSource(cachedGeoJson = CORRUPTED_GEOJSON),
            assetDataSource = FakeCyclewaysAssetDataSource(assetGeoJson = CORRUPTED_GEOJSON)
        )

        val result = repository.getCachedGeoJson()

        assertNull(result)
    }

    @Test
    fun `sync saves downloaded dataset after checksum validation`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(cachedGeoJson = null)
        val metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
            metadata = LocalCyclewaysMetadata(
                version = null,
                checksum = null
            )
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            metadataLocalDataSource = metadataLocalDataSource
        )

        val result = repository.sync()

        assertEquals(SyncCyclewaysResult.Updated, result)
        assertEquals(REMOTE_GEOJSON, fileDataSource.savedGeoJson)
        assertEquals(REMOTE_METADATA, metadataLocalDataSource.savedMetadata)
    }

    @Test
    fun `sync does not overwrite cache when dataset is already updated and cache is valid`() =
        runBlocking {
            val fileDataSource = FakeCyclewaysFileDataSource(
                cachedGeoJson = REMOTE_GEOJSON
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
            assertNull(fileDataSource.savedGeoJson)
        }

    @Test
    fun `sync redownloads when checksum is current but cache is corrupted`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CORRUPTED_GEOJSON
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

        assertEquals(SyncCyclewaysResult.Updated, result)
        assertEquals(REMOTE_GEOJSON, fileDataSource.savedGeoJson)
    }

    @Test
    fun `sync fails and preserves cache when downloaded checksum mismatches`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CACHED_GEOJSON
        )
        val metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
            metadata = LocalCyclewaysMetadata(
                version = null,
                checksum = null
            )
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            metadataLocalDataSource = metadataLocalDataSource,
            remoteDataSource = FakeCyclewaysRemoteDataSource(
                metadata = REMOTE_METADATA.copy(checksum = "wrong-checksum"),
                geoJson = REMOTE_GEOJSON
            )
        )

        val result = repository.sync()

        assertTrue(result is SyncCyclewaysResult.Failed)
        assertNull(fileDataSource.savedGeoJson)
        assertNull(metadataLocalDataSource.savedMetadata)
        assertEquals(CACHED_GEOJSON, fileDataSource.cachedGeoJson)
    }

    @Test
    fun `sync fails and preserves cache when downloaded geojson is invalid`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CACHED_GEOJSON
        )
        val metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
            metadata = LocalCyclewaysMetadata(
                version = null,
                checksum = null
            )
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            metadataLocalDataSource = metadataLocalDataSource,
            remoteDataSource = FakeCyclewaysRemoteDataSource(
                metadata = REMOTE_METADATA.copy(
                    checksum = Sha256Checksum.calculate(CORRUPTED_GEOJSON)
                ),
                geoJson = CORRUPTED_GEOJSON
            )
        )

        val result = repository.sync()

        assertTrue(result is SyncCyclewaysResult.Failed)
        assertNull(fileDataSource.savedGeoJson)
        assertNull(metadataLocalDataSource.savedMetadata)
        assertEquals(CACHED_GEOJSON, fileDataSource.cachedGeoJson)
    }

    @Test
    fun `sync fails and does not save metadata when atomic cache save fails`() = runBlocking {
        val fileDataSource = FakeCyclewaysFileDataSource(
            cachedGeoJson = CACHED_GEOJSON,
            failOnSave = true
        )
        val metadataLocalDataSource = FakeCyclewaysMetadataLocalDataSource(
            metadata = LocalCyclewaysMetadata(
                version = null,
                checksum = null
            )
        )
        val repository = repository(
            fileDataSource = fileDataSource,
            metadataLocalDataSource = metadataLocalDataSource
        )

        val result = repository.sync()

        assertTrue(result is SyncCyclewaysResult.Failed)
        assertNull(fileDataSource.savedGeoJson)
        assertNull(metadataLocalDataSource.savedMetadata)
        assertEquals(CACHED_GEOJSON, fileDataSource.cachedGeoJson)
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
        val cachedGeoJson: String?,
        private val failOnSave: Boolean = false
    ) : CyclewaysFileDataSource {

        var savedGeoJson: String? = null
            private set

        override suspend fun readGeoJson(): String? {
            return cachedGeoJson
        }

        override suspend fun saveGeoJson(content: String) {
            if (failOnSave) {
                throw IOException("Atomic save failed")
            }

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
        const val CORRUPTED_GEOJSON = """{"type":"FeatureCollection","features":"""
        const val REMOTE_GEOJSON = """{"type":"FeatureCollection","features":[{"id":"remote"}]}"""

        val REMOTE_METADATA = CyclewaysDatasetMetadata(
            version = "2026-05-12T15:47:46Z",
            lastUpdated = "2026-05-12T15:47:46Z",
            geoJsonUrl = "https://ciclo-guia.web.app/public-data/cycleways/latest.geojson",
            checksum = Sha256Checksum.calculate(REMOTE_GEOJSON),
            featureCount = 1,
            routeCount = 1
        )
    }
}
