package com.cicloguia.app.data.cycleways.remote

import com.cicloguia.app.core.network.HttpClient
import com.cicloguia.app.domain.cycleways.model.CyclewaysDatasetMetadata
import org.json.JSONObject
import javax.inject.Inject

class CyclewaysRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
) : CyclewaysRemoteDataSource {

    private companion object {
        const val METADATA_URL =
            "https://ciclo-guia.web.app/public-data/cycleways/metadata.json"
    }

    override suspend fun getMetadata(): CyclewaysDatasetMetadata {
        val json = httpClient.get(METADATA_URL)
        val root = JSONObject(json)

        return CyclewaysDatasetMetadata(
            version = root.getString("version"),
            lastUpdated = root.getString("lastUpdated"),
            geoJsonUrl = root.getString("geoJsonUrl"),
            checksum = root.getString("checksum"),
            featureCount = root.optInt("featureCount", 0),
            routeCount = root.optInt("routeCount", 0)
        )
    }

    override suspend fun getGeoJson(url: String): String {
        return httpClient.get(url)
    }
}