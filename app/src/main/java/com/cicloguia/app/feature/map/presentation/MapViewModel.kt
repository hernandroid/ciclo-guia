package com.cicloguia.app.feature.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.usecase.GetCachedCyclewaysGeoJsonUseCase
import com.cicloguia.app.feature.map.domain.usecase.GetMapStyleUrlUseCase
import com.cicloguia.app.feature.map.domain.usecase.SyncCyclewaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                    isSyncing = true
                )
            }

            when (val result = syncCyclewaysUseCase()) {
                SyncCyclewaysResult.AlreadyUpdated,
                SyncCyclewaysResult.Updated -> {
                    val latestGeoJson = getCachedCyclewaysGeoJsonUseCase()

                    _uiState.value = if (latestGeoJson != null) {
                        MapUiState.Content(
                            geoJson = latestGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false
                        )
                    } else {
                        MapUiState.Error(
                            message = "No se encontró información local de ciclovías"
                        )
                    }
                }

                is SyncCyclewaysResult.Failed -> {
                    _uiState.value = if (cachedGeoJson != null) {
                        MapUiState.Content(
                            geoJson = cachedGeoJson,
                            mapStyleUrl = mapStyleUrl,
                            isSyncing = false
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
}