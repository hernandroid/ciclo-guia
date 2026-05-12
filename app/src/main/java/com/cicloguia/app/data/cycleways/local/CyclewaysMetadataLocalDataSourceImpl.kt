package com.cicloguia.app.data.cycleways.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cicloguia.app.domain.cycleways.model.CyclewaysDatasetMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.cyclewaysDataStore by preferencesDataStore(
    name = "cycleways_dataset"
)

class CyclewaysMetadataLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CyclewaysMetadataLocalDataSource {

    private object Keys {
        val VERSION = stringPreferencesKey("version")
        val CHECKSUM = stringPreferencesKey("checksum")
    }

    override suspend fun getMetadata(): LocalCyclewaysMetadata {
        val preferences = context.cyclewaysDataStore.data.first()

        return LocalCyclewaysMetadata(
            version = preferences[Keys.VERSION],
            checksum = preferences[Keys.CHECKSUM]
        )
    }

    override suspend fun saveMetadata(metadata: CyclewaysDatasetMetadata) {
        context.cyclewaysDataStore.edit { preferences ->
            preferences[Keys.VERSION] = metadata.version
            preferences[Keys.CHECKSUM] = metadata.checksum
        }
    }
}