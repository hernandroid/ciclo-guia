package com.cicloguia.app.feature.map.presentation

import java.io.IOException

object MapErrorMessageResolver {

    fun noAvailableDataMessage(error: Throwable): String {
        return when {
            error.isDatasetError() -> DATASET_UNAVAILABLE_MESSAGE
            error is IOException -> NO_INTERNET_NO_DATA_MESSAGE
            else -> GENERIC_LOAD_ERROR_MESSAGE
        }
    }

    fun synchronizationFailedWithAvailableDataMessage(): String {
        return SYNC_FAILED_WITH_AVAILABLE_DATA_MESSAGE
    }

    fun unavailableDatasetMessage(): String {
        return DATASET_UNAVAILABLE_MESSAGE
    }

    private fun Throwable.isDatasetError(): Boolean {
        val message = message.orEmpty().lowercase()

        return message.contains("geojson") ||
            message.contains("checksum") ||
            message.contains("dataset")
    }

    const val NO_INTERNET_NO_DATA_MESSAGE =
        "No pudimos cargar las ciclovías. Revisa tu conexión e inténtalo de nuevo."

    const val SYNC_FAILED_WITH_AVAILABLE_DATA_MESSAGE =
        "No pudimos actualizar el mapa. Estás viendo los datos disponibles en este momento."

    const val DATASET_UNAVAILABLE_MESSAGE =
        "La información de ciclovías no está disponible por el momento. Inténtalo nuevamente más tarde."

    const val GENERIC_LOAD_ERROR_MESSAGE =
        "No pudimos cargar el mapa de ciclovías. Inténtalo nuevamente."
}
