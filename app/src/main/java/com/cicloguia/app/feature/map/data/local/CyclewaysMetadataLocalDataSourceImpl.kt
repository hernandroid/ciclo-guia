package com.cicloguia.app.feature.map.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.cicloguia.app.core.datastore.CyclewaysDataStore
import com.cicloguia.app.feature.map.domain.model.CyclewaysDatasetMetadata
import com.cicloguia.app.feature.map.domain.model.LocalCyclewaysMetadata
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CyclewaysMetadataLocalDataSourceImpl @Inject constructor(
    @CyclewaysDataStore
    private val dataStore: DataStore<Preferences>
) : CyclewaysMetadataLocalDataSource {

    override suspend fun getMetadata(): LocalCyclewaysMetadata {

        val preferences = dataStore.data.first()

        return LocalCyclewaysMetadata(
            version = preferences[CyclewaysMetadataKeys.Version],
            checksum = preferences[CyclewaysMetadataKeys.Checksum]
        )
    }

    override suspend fun saveMetadata(
        metadata: CyclewaysDatasetMetadata
    ) {

        dataStore.edit { preferences ->

            preferences[CyclewaysMetadataKeys.Version] =
                metadata.version

            preferences[CyclewaysMetadataKeys.Checksum] =
                metadata.checksum
        }
    }
}