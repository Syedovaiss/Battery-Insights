package com.ovais.batterymonitorer.feature.home.presentation

import com.ovais.batterymonitorer.feature.engine.InsightEngine
import com.ovais.batterymonitorer.feature.settings.domain.AppSettings
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity

data class BatteryState(
    val drainRate: Float = 0f,
    val insights: List<InsightEngine.BatteryInsight> = emptyList(),
    val isLoading: Boolean = true,
    val currentLevel: Int = 0,
    val temperature: Float = 0f,
    val healthPredictionRes: Int = 0,
    val healthPredictionArgs: List<Any> = emptyList(),
    val simulatorScreenTimeHours: Float = 5f,
    val simulatorLocationOn: Boolean = true,
    val simulatedSavings: Int = 0,
    val history: List<Float> = emptyList(), // Battery levels for the graph
    val recentReports: List<DailyReportEntity> = emptyList(),
    val timeToEmptyMinutes: Int = -1,
    val timeToFullMinutes: Int = -1,
    val isCharging: Boolean = false,
    val simulatorBrightness: Float = 50f,
    val simulatorNetwork5G: Boolean = true,
    val ecoAdviceRes: Int = 0,
    val ecoAdviceArgs: List<Any> = emptyList(),
    val exportStatus: ExportStatus = ExportStatus.IDLE,
    val reportFilter: ReportFilter = ReportFilter.DAILY,
    val batteryHealthScore: Int = 100,
    val currentNow: Int = 0,
    val voltage: Int = 0,
    val activeChargeSessionMinutes: Int = 0,
    val activeChargeSessionGainPercent: Int = 0,
    val activeChargeSpeedPercentPerHour: Float = 0f,
    val settings: AppSettings = AppSettings()
)

enum class ExportStatus {
    IDLE, LOADING, SUCCESS, ERROR
}

enum class ReportFilter {
    DAILY, WEEKLY
}

sealed class BatteryIntent {
    object Load : BatteryIntent()
    object ExportData : BatteryIntent()
    data class UpdateSimulatorScreenTime(val hours: Float) : BatteryIntent()
    data class UpdateSimulatorLocation(val isOn: Boolean) : BatteryIntent()
    data class UpdateSimulatorBrightness(val percent: Float) : BatteryIntent()
    data class UpdateSimulatorNetwork(val is5G: Boolean) : BatteryIntent()
    data class ChangeReportFilter(val filter: ReportFilter) : BatteryIntent()
}
