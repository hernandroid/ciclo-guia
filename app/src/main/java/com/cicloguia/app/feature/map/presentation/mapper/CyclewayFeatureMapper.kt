package com.cicloguia.app.feature.map.presentation.mapper

import com.cicloguia.app.feature.map.presentation.formatter.CyclewayTextFormatter
import com.cicloguia.app.feature.map.presentation.model.CyclewayGeoJsonProperty
import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi
import org.maplibre.geojson.Feature

fun Feature.toSelectedCyclewayUi(): SelectedCyclewayUi {
    return SelectedCyclewayUi(
        objectId = propertyOrFallback(CyclewayGeoJsonProperty.OBJECT_ID),
        code = propertyOrFallback(CyclewayGeoJsonProperty.CODE),
        status = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.STATUS)
        ),
        district = CyclewayTextFormatter.formatTitleCase(
            propertyOrFallback(CyclewayGeoJsonProperty.DISTRICT)
        ),
        name = CyclewayTextFormatter.formatTitleCase(
            propertyOrFallback(
                key = CyclewayGeoJsonProperty.NAME,
                fallback = DEFAULT_CYCLEWAY_NAME
            )
        ),
        section = CyclewayTextFormatter.formatTitleCase(
            propertyOrFallback(CyclewayGeoJsonProperty.SECTION)
        ),
        roadType = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.ROAD_TYPE)
        ),
        cyclewayPosition = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.CYCLEWAY_POSITION)
        ),
        lengthKm = CyclewayTextFormatter.formatLengthKm(
            propertyOrFallback(CyclewayGeoJsonProperty.LENGTH)
        ),
        direction = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.DIRECTION)
        ),
        segregationType = CyclewayTextFormatter.formatSegregationType(
            propertyOrFallback(CyclewayGeoJsonProperty.SEGREGATION_TYPE)
        ),
        lighting = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.LIGHTING)
        ),
        surveillance = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.SURVEILLANCE)
        ),
        projectName = CyclewayTextFormatter.formatTitleCase(
            propertyOrFallback(CyclewayGeoJsonProperty.PROJECT_NAME)
        ),
        quantity = propertyOrFallback(CyclewayGeoJsonProperty.QUANTITY),
        implementationType = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.IMPLEMENTATION_TYPE)
        ),
        year = propertyOrFallback(CyclewayGeoJsonProperty.YEAR),
        authorityType = CyclewayTextFormatter.formatText(
            propertyOrFallback(CyclewayGeoJsonProperty.AUTHORITY_TYPE)
        ),
        creationDate = propertyOrFallback(CyclewayGeoJsonProperty.CREATION_DATE)
    )
}

private fun Feature.propertyOrFallback(
    key: String,
    fallback: String = UNKNOWN_VALUE
): String {
    val property = getProperty(key) ?: return fallback

    return property.asString
        .trim()
        .takeIf { it.isNotBlank() }
        ?: fallback
}

private const val UNKNOWN_VALUE = "No especificado"
private const val DEFAULT_CYCLEWAY_NAME = "Ciclovía seleccionada"