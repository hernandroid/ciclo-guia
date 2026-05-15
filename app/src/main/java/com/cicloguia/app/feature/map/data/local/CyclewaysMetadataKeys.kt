package com.cicloguia.app.feature.map.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object CyclewaysMetadataKeys {

    val Version = stringPreferencesKey("version")

    val Checksum = stringPreferencesKey("checksum")
}