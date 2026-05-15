package com.cicloguia.app.feature.map.domain.model

data class CyclewaysDatasetMetadata(
    val version: String,
    val lastUpdated: String,
    val geoJsonUrl: String,
    val checksum: String,
    val featureCount: Int,
    val routeCount: Int
)