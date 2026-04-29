package com.ovais.batterymonitorer.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovais.batterymonitorer.core.telemetry.AppTelemetry
import com.ovais.batterymonitorer.feature.settings.data.SettingsExportManager
import com.ovais.batterymonitorer.feature.settings.data.SettingsRepository
import com.ovais.batterymonitorer.feature.settings.domain.AppSettings
import com.ovais.batterymonitorer.worker.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val exportManager: SettingsExportManager,
    private val scheduler: WorkManagerScheduler
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _exportStatus = MutableStateFlow(SettingsExportStatus.IDLE)
    val exportStatus: StateFlow<SettingsExportStatus> = _exportStatus.asStateFlow()

    fun updateLowBatteryThreshold(value: Int) {
        viewModelScope.launch { repository.updateLowBatteryThreshold(value) }
    }

    fun updateHighTempThreshold(value: Float) {
        viewModelScope.launch { repository.updateHighTempThreshold(value) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateNotificationsEnabled(enabled) }
    }

    fun updateAccentColor(color: Long) {
        viewModelScope.launch { repository.updateAccentColor(color) }
    }

    fun updatePollingInterval(minutes: Int) {
        viewModelScope.launch { 
            repository.updatePollingInterval(minutes)
            scheduler.scheduleBatteryPolling(minutes)
            AppTelemetry.breadcrumb("setting_polling_interval", mapOf("minutes" to minutes.toString()))
        }
    }

    fun updateAIEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateAIEnabled(enabled) }
    }

    fun updateHistoryPointsCount(count: Int) {
        viewModelScope.launch { repository.updateHistoryPointsCount(count) }
    }

    fun updateAutoDeleteHistoryDays(days: Int) {
        viewModelScope.launch { repository.updateAutoDeleteHistoryDays(days) }
    }

    fun updateDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateDarkThemeEnabled(enabled) }
    }

    fun updateDynamicColorsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateDynamicColorsEnabled(enabled) }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
            scheduler.scheduleBatteryPolling(15)
        }
    }

    fun updateChargeLimitEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateChargeLimitEnabled(enabled) }
    }

    fun updateChargeLimitPercent(percent: Int) {
        viewModelScope.launch { repository.updateChargeLimitPercent(percent) }
    }

    fun exportData(format: ExportFormat) {
        viewModelScope.launch {
            _exportStatus.value = SettingsExportStatus.LOADING
            AppTelemetry.breadcrumb("export_started", mapOf("format" to format.name))
            try {
                exportManager.export(format)

                _exportStatus.value = SettingsExportStatus.SUCCESS
                AppTelemetry.breadcrumb("export_success", mapOf("format" to format.name))
                delay(2000)
                _exportStatus.value = SettingsExportStatus.IDLE
            } catch (e: Exception) {
                _exportStatus.value = SettingsExportStatus.ERROR
                AppTelemetry.recordException(e, "export_failed_${format.name.lowercase()}")
            }
        }
    }
}

enum class ExportFormat {
    CSV, JSON, PDF
}

enum class SettingsExportStatus {
    IDLE, LOADING, SUCCESS, ERROR
}
