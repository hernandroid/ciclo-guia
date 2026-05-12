package com.cicloguia.app.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cicloguia.app.domain.cycleways.model.SyncCyclewaysResult
import com.cicloguia.app.domain.cycleways.usecase.GetCachedCyclewaysGeoJsonUseCase
import com.cicloguia.app.domain.cycleways.usecase.SyncCyclewaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCachedCyclewaysGeoJsonUseCase: GetCachedCyclewaysGeoJsonUseCase,
    private val syncCyclewaysUseCase: SyncCyclewaysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadCycleways()
    }

    fun onReportClicked() {
        // Próximo paso: abrir bottom sheet o navegar a ReportScreen.
    }

    private fun loadCycleways() {
        viewModelScope.launch {
            val cachedGeoJson = getCachedCyclewaysGeoJsonUseCase()

            if (cachedGeoJson != null) {
                _uiState.value = MapUiState.Success(
                    geoJson = cachedGeoJson,
                    isSyncing = true
                )
            }

            when (val result = syncCyclewaysUseCase()) {
                SyncCyclewaysResult.AlreadyUpdated,
                SyncCyclewaysResult.Updated -> {
                    val latestGeoJson = getCachedCyclewaysGeoJsonUseCase()

                    _uiState.value = if (latestGeoJson != null) {
                        MapUiState.Success(
                            geoJson = latestGeoJson,
                            isSyncing = false
                        )
                    } else {
                        MapUiState.Error("No se encontró información local de ciclovías")
                    }
                }

                is SyncCyclewaysResult.Failed -> {
                    if (cachedGeoJson == null) {
                        _uiState.value = MapUiState.Error(
                            result.error.message ?: "No se pudieron cargar las ciclovías"
                        )
                    } else {
                        _uiState.value = MapUiState.Success(
                            geoJson = cachedGeoJson,
                            isSyncing = false
                        )
                    }
                }
            }
        }
    }
}