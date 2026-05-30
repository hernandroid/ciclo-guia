package com.cicloguia.app.feature.map.presentation

object MapDataSourceMessageResolver {

    fun indicatorMessage(
        dataSource: MapDataSourceUi,
        isSyncing: Boolean
    ): String? {
        return when (dataSource) {
            MapDataSourceUi.Updated -> {
                if (isSyncing) {
                    "Actualizando ciclovías..."
                } else {
                    null
                }
            }

            MapDataSourceUi.DownloadedCache -> {
                if (isSyncing) {
                    "Mostrando datos guardados mientras actualizamos."
                } else {
                    "Mostrando datos guardados."
                }
            }

            MapDataSourceUi.EmbeddedAsset -> {
                if (isSyncing) {
                    "Mostrando datos incluidos mientras actualizamos."
                } else {
                    "Mostrando datos incluidos sin conexión."
                }
            }
        }
    }
}
