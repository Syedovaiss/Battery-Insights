package com.ovais.batterymonitorer.feature.home.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovais.batterymonitorer.R
import com.ovais.batterymonitorer.feature.engine.InsightEngine
import com.ovais.batterymonitorer.feature.home.data.BatteryRepository
import com.ovais.batterymonitorer.feature.settings.data.SettingsRepository
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val repo: BatteryRepository,
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BatteryState())
    val state: StateFlow<BatteryState> = _state.asStateFlow()

    init {
        process(BatteryIntent.Load)
    }

    fun process(intent: BatteryIntent) {
        when (intent) {
            BatteryIntent.Load -> {
                refresh()
                observe()
            }
            is BatteryIntent.UpdateSimulatorLocation -> {
                _state.value = _state.value.copy(simulatorLocationOn = intent.isOn)
                updateSimulatedSavings()
            }
            is BatteryIntent.UpdateSimulatorScreenTime -> {
                _state.value = _state.value.copy(simulatorScreenTimeHours = intent.hours)
                updateSimulatedSavings()
            }
            is BatteryIntent.UpdateSimulatorBrightness -> {
                _state.value = _state.value.copy(simulatorBrightness = intent.percent)
                updateSimulatedSavings()
            }
            is BatteryIntent.UpdateSimulatorNetwork -> {
                _state.value = _state.value.copy(simulatorNetwork5G = intent.is5G)
                updateSimulatedSavings()
            }
            is BatteryIntent.ChangeReportFilter -> {
                _state.value = _state.value.copy(reportFilter = intent.filter)
            }
            BatteryIntent.ExportData -> exportData()
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(exportStatus = ExportStatus.LOADING)
            try {
                val batteryData = repo.getAllOnce()
                val reports = repo.getAllReportsOnce()

                val csvContent = buildString {
                    append("Type,Timestamp/Date,Level/TotalDrain,Temp/AvgTemp,Charging/Signal,ScreenOn/SOT\n")
                    batteryData.forEach {
                        append("Entry,${it.timestamp},${it.level},${it.temperature},${it.isCharging},${it.isScreenOn}\n")
                    }
                    reports.forEach {
                        append("Report,${it.date},${it.totalDrain},${it.avgTemp},${it.avgSignalStrength},${it.screenOnTimeMinutes}\n")
                    }
                }

                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val file = File(exportDir, "battery_history_${System.currentTimeMillis()}.csv")
                file.writeText(csvContent)

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Export Battery Data").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)

                _state.value = _state.value.copy(exportStatus = ExportStatus.SUCCESS)
                kotlinx.coroutines.delay(2000)
                _state.value = _state.value.copy(exportStatus = ExportStatus.IDLE)
            } catch (e: Exception) {
                _state.value = _state.value.copy(exportStatus = ExportStatus.ERROR)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            repo.refreshBatteryState()
        }
    }

    private fun observe() {
        viewModelScope.launch {
            settingsRepo.settingsFlow.collectLatest { settings ->
                _state.value = _state.value.copy(settings = settings)
                updateEcoAdvice()
            }
        }

        viewModelScope.launch {
            repo.observeBattery().collect { list ->
                if (list.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false)
                    return@collect
                }

                val insights = InsightEngine.generateInsights(list, _state.value.settings)
                val latest = list.first()
                
                // Populating history for the graph using the configured point count
                val history = list.take(_state.value.settings.historyPointsCount).map { it.level / 100f }.reversed()

                val healthMonths = predictHealthMonths(list)
                val isCharging = latest.isCharging

                val timeToEmpty = if (!isCharging) calculateTimeToEmpty(list) else -1
                val timeToFull = if (isCharging) calculateTimeToFull(list) else -1
                val batteryHealthScore = calculateBatteryHealthScore(list)
                val chargeSessionStats = calculateChargeSessionStats(list)

                _state.value = _state.value.copy(
                    drainRate = calculateDrainRate(list),
                    insights = insights,
                    isLoading = false,
                    currentLevel = latest.level.toInt(),
                    temperature = latest.temperature,
                    healthPredictionRes = R.string.health_drop_prediction,
                    healthPredictionArgs = listOf(healthMonths),
                    history = history,
                    isCharging = isCharging,
                    timeToEmptyMinutes = timeToEmpty,
                    timeToFullMinutes = timeToFull,
                    batteryHealthScore = batteryHealthScore,
                    activeChargeSessionMinutes = chargeSessionStats.sessionMinutes,
                    activeChargeSessionGainPercent = chargeSessionStats.gainPercent,
                    activeChargeSpeedPercentPerHour = chargeSessionStats.speedPerHour,
                )
                updateEcoAdvice()
            }
        }

        viewModelScope.launch {
            repo.observeRecentReports().collect { reports ->
                _state.value = _state.value.copy(recentReports = reports)
            }
        }
    }

    private fun updateEcoAdvice() {
        val s = _state.value
        val advice: Pair<Int, List<Any>> = when {
            s.temperature > s.settings.highTempThreshold -> R.string.eco_advice_heat to emptyList()
            s.currentLevel < s.settings.lowBatteryThreshold -> R.string.eco_advice_low to listOf(s.settings.lowBatteryThreshold)
            else -> R.string.eco_advice_good to emptyList()
        }
        _state.value = s.copy(ecoAdviceRes = advice.first, ecoAdviceArgs = advice.second)
    }

    private fun calculateTimeToEmpty(list: List<BatteryEntity>): Int {
        val rate = calculateDrainRate(list)
        if (rate <= 0) return -1
        val latestLevel = list.first().level
        // Use exponential moving average for smoother prediction if available, 
        // but for now, simple rate is fine as long as it's accurate.
        return ((latestLevel / rate) * 60).toInt()
    }

    private fun calculateTimeToFull(list: List<BatteryEntity>): Int {
        val chargingData = list.filter { it.isCharging }.take(10)
        if (chargingData.size < 2) return -1
        
        val latest = chargingData.first()
        val oldest = chargingData.last()
        val durationHours = (latest.timestamp - oldest.timestamp) / (1000f * 60 * 60f)
        val chargeGained = latest.level - oldest.level
        
        if (chargeGained <= 0 || durationHours <= 0) return -1
        
        val chargeRate = chargeGained / durationHours
        val remainingCharge = 100f - latest.level
        return ((remainingCharge / chargeRate) * 60).toInt()
    }

    private fun updateSimulatedSavings() {
        val currentState = _state.value
        
        // Base discharge rates (per hour - in percentage)
        val screenBaseRate = 5f 
        val idleBaseRate = 1.0f
        val locationPenalty = 2.0f
        val brightnessPenalty = (currentState.simulatorBrightness / 100f) * 5f
        val networkPenalty = if (currentState.simulatorNetwork5G) 3.0f else 0.5f
        
        val currentLocationImpact = if (currentState.simulatorLocationOn) locationPenalty else 0f
        
        // Current hourly drain based on sliders
        val currentHourlyScreenDrain = screenBaseRate + currentLocationImpact + brightnessPenalty + networkPenalty
        val currentHourlyIdleDrain = idleBaseRate + (currentLocationImpact * 0.5f) + (networkPenalty * 0.2f)
        
        val currentDailyDrain = (currentState.simulatorScreenTimeHours * currentHourlyScreenDrain) + 
                                ((24 - currentState.simulatorScreenTimeHours) * currentHourlyIdleDrain)
        
        // Target: Ideal optimized profile
        val targetScreenTime = 3f
        val targetBrightnessPenalty = 0.5f // Low brightness
        val targetNetworkPenalty = 0.5f // 4G/WiFi
        val targetLocationImpact = 0f // Location off
        
        val optimizedHourlyScreenDrain = screenBaseRate + targetLocationImpact + targetBrightnessPenalty + targetNetworkPenalty
        val optimizedHourlyIdleDrain = idleBaseRate + (targetLocationImpact * 0.5f) + (targetNetworkPenalty * 0.2f)
        
        val optimizedDailyDrain = (targetScreenTime * optimizedHourlyScreenDrain) + 
                                ((24 - targetScreenTime) * optimizedHourlyIdleDrain)
        
        val totalSavings = (currentDailyDrain - optimizedDailyDrain).toInt().coerceAtLeast(0)
        
        _state.value = currentState.copy(simulatedSavings = totalSavings)
    }

    private fun calculateDrainRate(list: List<BatteryEntity>): Float {
        if (list.size < 2) return 0f
        // Use a 2-hour window or all available if less
        val latest = list.first()
        val window = list.filter { latest.timestamp - it.timestamp < 2 * 60 * 60 * 1000 }
        val oldest = if (window.size >= 2) window.last() else list.last()
        
        val hours = (latest.timestamp - oldest.timestamp) / (1000f * 60 * 60f)
        return if (hours > 0.1) (oldest.level - latest.level) / hours else 0f
    }

    private fun predictHealthMonths(list: List<BatteryEntity>): Int {
        val highTempDays = list.count { it.temperature > 40 }
        return if (highTempDays > 10) 10 else 18
    }

    private fun calculateBatteryHealthScore(list: List<BatteryEntity>): Int {
        if (list.isEmpty()) return 100
        val sample = list.take(200)
        val avgTemp = sample.map { it.temperature }.average().toFloat()
        val highTempRatio = sample.count { it.temperature >= 40f }.toFloat() / sample.size.toFloat()
        val deepDischargeRatio = sample.count { it.level <= 15f }.toFloat() / sample.size.toFloat()
        val score = 100f - (highTempRatio * 35f) - (deepDischargeRatio * 25f) - ((avgTemp - 32f).coerceAtLeast(0f) * 1.2f)
        return score.toInt().coerceIn(55, 100)
    }

    private fun calculateChargeSessionStats(list: List<BatteryEntity>): ChargeSessionStats {
        val chargingRun = list.takeWhile { it.isCharging }.reversed()
        if (chargingRun.size < 2) return ChargeSessionStats()
        val start = chargingRun.first()
        val end = chargingRun.last()
        val durationMs = end.timestamp - start.timestamp
        if (durationMs <= 0) return ChargeSessionStats()

        val gain = (end.level - start.level).coerceAtLeast(0f)
        val hours = durationMs / (1000f * 60f * 60f)
        val speed = if (hours > 0.05f) gain / hours else 0f
        return ChargeSessionStats(
            sessionMinutes = (durationMs / 60000L).toInt().coerceAtLeast(0),
            gainPercent = gain.toInt().coerceAtLeast(0),
            speedPerHour = speed
        )
    }

    private data class ChargeSessionStats(
        val sessionMinutes: Int = 0,
        val gainPercent: Int = 0,
        val speedPerHour: Float = 0f
    )
}
