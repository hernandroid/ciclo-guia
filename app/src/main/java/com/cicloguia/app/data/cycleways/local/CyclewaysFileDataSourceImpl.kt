package com.cicloguia.app.data.cycleways.local

import android.content.Context
import com.cicloguia.app.core.storage.CyclewaysFileDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CyclewaysFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CyclewaysFileDataSource {

    private val file: File
        get() = File(context.filesDir, "cycleways/latest.geojson")

    override suspend fun readGeoJson(): String? = withContext(Dispatchers.IO) {
        if (file.exists()) file.readText() else null
    }

    override suspend fun saveGeoJson(content: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}