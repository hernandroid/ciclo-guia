package com.cicloguia.app.feature.map.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CyclewaysAssetDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CyclewaysAssetDataSource {

    override suspend fun readGeoJson(): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(ASSET_FILE_NAME).bufferedReader().use { reader ->
                reader.readText()
            }
        }.getOrNull()
    }

    private companion object {
        const val ASSET_FILE_NAME = "ciclovias.geojson"
    }
}
