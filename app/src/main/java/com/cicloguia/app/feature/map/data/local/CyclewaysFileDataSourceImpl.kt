package com.cicloguia.app.feature.map.data.local

import android.content.Context
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CyclewaysFileDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CyclewaysFileDataSource {

    private val file: File
        get() = File(context.filesDir, "cycleways/latest.geojson")

    override suspend fun readGeoJson(): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (file.exists()) file.readText() else null
        }.getOrNull()
    }

    override suspend fun saveGeoJson(content: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()

        val atomicFile = AtomicFile(file)
        val stream = atomicFile.startWrite()

        try {
            stream.write(content.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
        } catch (error: Throwable) {
            atomicFile.failWrite(stream)
            throw error
        }
    }
}
