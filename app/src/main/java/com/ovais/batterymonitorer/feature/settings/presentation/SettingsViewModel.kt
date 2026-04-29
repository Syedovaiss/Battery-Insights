package com.ovais.batterymonitorer.feature.settings.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovais.batterymonitorer.feature.home.data.BatteryRepository
import com.ovais.batterymonitorer.feature.settings.data.SettingsRepository
import com.ovais.batterymonitorer.feature.settings.domain.AppSettings
import com.ovais.batterymonitorer.worker.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val batteryRepository: BatteryRepository,
    @ApplicationContext private val context: Context,
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
        }
    }

    fun updateAIEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateAIEnabled(enabled) }
    }

    fun updateHistoryPointsCount(count: Int) {
        viewModelScope.launch { repository.updateHistoryPointsCount(count) }
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
            try {
                val batteryData = batteryRepository.getAllOnce()
                val reports = batteryRepository.getAllReportsOnce()

                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val timestamp = System.currentTimeMillis()

                val file = when (format) {
                    ExportFormat.CSV -> {
                        File(exportDir, "battery_history_$timestamp.csv").apply {
                            writeText(
                                buildString {
                                    append("Type,Timestamp/Date,Level/TotalDrain,Temp/AvgTemp,Charging/Signal,ScreenOn/SOT\n")
                                    batteryData.forEach {
                                        append("Entry,${it.timestamp},${it.level},${it.temperature},${it.isCharging},${it.isScreenOn}\n")
                                    }
                                    reports.forEach {
                                        append("Report,${it.date},${it.totalDrain},${it.avgTemp},${it.avgSignalStrength},${it.screenOnTimeMinutes}\n")
                                    }
                                }
                            )
                        }
                    }
                    ExportFormat.JSON -> {
                        File(exportDir, "battery_history_$timestamp.json").apply {
                            writeText(
                                buildString {
                                    append("{\n")
                                    append("  \"batteryEntries\": [\n")
                                    batteryData.forEachIndexed { index, item ->
                                        append("    {\"timestamp\": ${item.timestamp}, \"level\": ${item.level}, \"temperature\": ${item.temperature}, \"isCharging\": ${item.isCharging}, \"isScreenOn\": ${item.isScreenOn}, \"signalStrength\": ${item.signalStrength}}")
                                        append(if (index == batteryData.lastIndex) "\n" else ",\n")
                                    }
                                    append("  ],\n")
                                    append("  \"dailyReports\": [\n")
                                    reports.forEachIndexed { index, report ->
                                        append("    {\"date\": \"${report.date}\", \"avgTemp\": ${report.avgTemp}, \"totalDrain\": ${report.totalDrain}, \"avgSignalStrength\": ${report.avgSignalStrength}, \"screenOnTimeMinutes\": ${report.screenOnTimeMinutes}}")
                                        append(if (index == reports.lastIndex) "\n" else ",\n")
                                    }
                                    append("  ]\n")
                                    append("}\n")
                                }
                            )
                        }
                    }
                    ExportFormat.PDF -> {
                        File(exportDir, "battery_history_$timestamp.pdf").apply {
                            val doc = PdfDocument()
                            val paint = Paint().apply { textSize = 11f }
                            var pageNumber = 1
                            var page = doc.startPage(
                                PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            )
                            var canvas = page.canvas
                            var y = 32

                            fun newPage() {
                                doc.finishPage(page)
                                pageNumber += 1
                                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                                canvas = page.canvas
                                y = 32
                            }

                            fun drawLine(text: String) {
                                if (y > 810) newPage()
                                canvas.drawText(text.take(100), 24f, y.toFloat(), paint)
                                y += 16
                            }

                            drawLine("Battery Insight Export")
                            drawLine("Generated: $timestamp")
                            y += 8
                            drawLine("Battery Entries (${batteryData.size})")
                            batteryData.forEach {
                                drawLine("t=${it.timestamp}  level=${it.level.toInt()}%  temp=${it.temperature}C  charging=${it.isCharging}")
                            }
                            y += 8
                            drawLine("Daily Reports (${reports.size})")
                            reports.forEach {
                                drawLine("date=${it.date} drain=${it.totalDrain}% temp=${it.avgTemp}C sot=${it.screenOnTimeMinutes}m")
                            }

                            doc.finishPage(page)
                            outputStream().use { doc.writeTo(it) }
                            doc.close()
                        }
                    }
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mime = when (format) {
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.PDF -> "application/pdf"
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Export Battery Data").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)

                _exportStatus.value = SettingsExportStatus.SUCCESS
                delay(2000)
                _exportStatus.value = SettingsExportStatus.IDLE
            } catch (_: Exception) {
                _exportStatus.value = SettingsExportStatus.ERROR
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
