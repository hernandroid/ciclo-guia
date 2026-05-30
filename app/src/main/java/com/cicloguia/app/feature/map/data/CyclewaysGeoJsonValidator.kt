package com.cicloguia.app.feature.map.data

import com.google.gson.JsonParser

object CyclewaysGeoJsonValidator {

    fun isValid(content: String): Boolean {
        return runCatching {
            val root = JsonParser
                .parseString(content)
                .asJsonObject

            root.get(TYPE_KEY)?.asString == FEATURE_COLLECTION_TYPE &&
                root.get(FEATURES_KEY)?.isJsonArray == true
        }.getOrDefault(false)
    }

    private const val TYPE_KEY = "type"
    private const val FEATURES_KEY = "features"
    private const val FEATURE_COLLECTION_TYPE = "FeatureCollection"
}
