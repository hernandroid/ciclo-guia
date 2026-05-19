package com.cicloguia.app.feature.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.usecase.GetCachedCyclewaysGeoJsonUseCase
import com.cicloguia.app.feature.map.domain.usecase.GetMapStyleUrlUseCase
import com.cicloguia.app.feature.map.domain.usecase.SyncCyclewaysUseCase
import com.cicloguia.app.feature.map.presentation.model.CyclewayLegendUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCachedCyclewaysGeoJsonUseCase: GetCachedCyclewaysGeoJsonUseCase,
    private val syncCyclewaysUseCase: SyncCyclewaysUseCase,
    private val getMapStyleUrlUseCase: GetMapStyleUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MapUiEffect>()
    val effect: SharedFlow<MapUiEffect> = _effect.asSharedFlow()

    init {
        loadCycleways()
    }

    fun onEvent(event: MapUiEvent) {
        when (event) {
            MapUiEvent.ReportClicked -> {
                viewModelScope.launch {
                    _effect.emit(MapUiEffect.NavigateToReport)
                }
            }

            MapUiEvent.RetryClicked -> {
                loadCycleways()
            }

            MapUiEvent.CenterOnUserLocationClicked -> {
                updateContentState { currentState ->
                    currentState.copy(
                        centerOnUserLocationRequest = currentState.centerOnUserLocationRequest + 1
                    )
                }
            }

            MapUiEvent.CameraCenteredOnUserLocation -> {
                updateContentState { currentState ->
                    currentState.copy(
                        isFollowingUserLocation = true
                    )
                }
            }

            MapUiEvent.MapMovedByUser -> {
                updateContentState { currentState ->
                    currentState.copy(
                        isFollowingUserLocation = false
                    )
                }
            }

            is MapUiEvent.CyclewayClicked -> {
                updateContentState { currentState ->
                    currentState.copy(
                        selectedCyclewayName = event.cycleway.name,
                        selectedCycleway = event.cycleway
                    )
                }
            }

            MapUiEvent.DismissSelectedCycleway -> {
                updateContentState { currentState ->
                    currentState.copy(
                        selectedCyclewayName = DEFAULT_SELECTED_CYCLEWAY_NAME,
                        selectedCycleway = null
                    )
                }
            }
        }
    }

    private fun loadCycleways() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading

            val mapStyleUrl = getMapStyleUrlUseCase()
            val cachedGeoJson = getCachedCyclewaysGeoJsonUseCase()

            if (cachedGeoJson != null) {
                _uiState.value = MapUiState.Content(
                    geoJson = cachedGeoJson,
                    mapStyleUrl = mapStyleUrl,
                    isSyncing = true,
                    legend = buildLegendFromGeoJson(cachedGeoJson)
                )
            }

            when (val result = syncCyclewaysUseCase()) {
                SyncCyclewaysResult.AlreadyUpdated,
                SyncCyclewaysResult.Updated -> {
                    val latestGeoJson = getCachedCyclewaysGeoJsonUseCase()

                    _uiState.value = if (latestGeoJson != null) {
                        val latestLegend = buildLegendFromGeoJson(latestGeoJson)
                        val currentContent = _uiState.value as? MapUiState.Content

                        currentContent?.copy(
                            geoJson = latestGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false,
                            legend = latestLegend
                        ) ?: MapUiState.Content(
                            geoJson = latestGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false,
                            legend = latestLegend
                        )
                    } else {
                        MapUiState.Error(
                            message = "No se encontró información local de ciclovías"
                        )
                    }
                }

                is SyncCyclewaysResult.Failed -> {
                    _uiState.value = if (cachedGeoJson != null) {
                        val cachedLegend = buildLegendFromGeoJson(cachedGeoJson)
                        val currentContent = _uiState.value as? MapUiState.Content

                        currentContent?.copy(
                            geoJson = cachedGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false,
                            legend = cachedLegend
                        ) ?: MapUiState.Content(
                            geoJson = cachedGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false,
                            legend = cachedLegend
                        )
                    } else {
                        MapUiState.Error(
                            message = result.error.message
                                ?: "No se pudieron cargar las ciclovías"
                        )
                    }
                }
            }
        }
    }

    private fun updateContentState(
        update: (MapUiState.Content) -> MapUiState.Content
    ) {
        val currentState = _uiState.value

        if (currentState is MapUiState.Content) {
            _uiState.value = update(currentState)
        }
    }

    private fun buildLegendFromGeoJson(
        geoJson: String
    ): CyclewayLegendUi {
        return runCatching {
            val features = JSONObject(geoJson).optJSONArray("features")
                ?: return CyclewayLegendUi()

            var existingCount = 0
            var plannedCount = 0
            var underConstructionCount = 0

            for (index in 0 until features.length()) {
                val feature = features.optJSONObject(index) ?: continue
                val properties = feature.optJSONObject("properties") ?: continue

                when (properties.optString("ESTADO").normalizeState()) {
                    STATE_EXISTING -> existingCount++
                    STATE_PLANNED -> plannedCount++
                    STATE_UNDER_CONSTRUCTION -> underConstructionCount++
                }
            }

            CyclewayLegendUi(
                existingCount = existingCount,
                plannedCount = plannedCount,
                underConstructionCount = underConstructionCount
            )
        }.getOrDefault(CyclewayLegendUi())
    }

    private fun String.normalizeState(): String {
        return trim()
            .uppercase()
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
    }

    private companion object {
        const val DEFAULT_SELECTED_CYCLEWAY_NAME = "Ciclovías de Lima"

        const val STATE_EXISTING = "EXISTENTE"
        const val STATE_PLANNED = "EN PROYECTO"
        const val STATE_UNDER_CONSTRUCTION = "EN EJECUCION"
    }
}