package com.cicloguia.app.feature.map.presentation.formatter

import java.util.Locale

object CyclewayTextFormatter {

    fun formatLengthKm(value: String): String {
        val numericValue = value.toDoubleOrNull()

        return if (numericValue != null && numericValue > 0.0) {
            String.format(Locale.US, "%.2f km", numericValue)
        } else {
            LENGTH_NOT_AVAILABLE
        }
    }

    fun formatText(value: String): String {
        if (value.isBlank()) return UNKNOWN_VALUE

        return value.lowercase()
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
    }

    fun formatTitleCase(value: String): String {
        if (value.isBlank()) return UNKNOWN_VALUE

        return value.lowercase()
            .split(" ")
            .filter { it.isNotBlank() }
            .mapIndexed { index, word ->
                if (index != 0 && word in lowercaseConnectors) {
                    word
                } else {
                    word.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                }
            }
            .joinToString(" ")
    }

    fun formatSegregationType(value: String): String {
        if (value.isBlank() || value.equals(UNKNOWN_VALUE, ignoreCase = true)) {
            return UNKNOWN_VALUE
        }

        val values = value.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { formatText(it).lowercase() }
            .distinct()

        return when (values.size) {
            0 -> UNKNOWN_VALUE
            1 -> values.first().capitalizeFirstChar()
            2 -> "${values[0].capitalizeFirstChar()} y ${values[1]}"
            else -> {
                val firstValues = values.dropLast(1)
                val lastValue = values.last()
                "${firstValues.joinToString(", ").capitalizeFirstChar()} y $lastValue"
            }
        }
    }

    private fun String.capitalizeFirstChar(): String {
        return replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }

    private val lowercaseConnectors = setOf(
        "de", "del", "la", "las", "los", "y", "e", "en", "a"
    )

    private const val UNKNOWN_VALUE = "No especificado"
    private const val LENGTH_NOT_AVAILABLE = "No disponible"
}