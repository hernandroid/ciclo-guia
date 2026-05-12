package com.cicloguia.app.domain.cycleways.model

data class CyclewaysDatasetMetadata(
    val version: String,
    val lastUpdated: String,
    val geoJsonUrl: String,
    val checksum: String,
    val featureCount: Int,
    val routeCount: Int
)