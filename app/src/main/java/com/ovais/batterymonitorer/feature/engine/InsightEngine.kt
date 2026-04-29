package com.ovais.batterymonitorer.feature.engine

import com.ovais.batterymonitorer.R
import com.ovais.batterymonitorer.feature.settings.domain.AppSettings
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import java.util.Calendar
import kotlin.math.pow
import kotlin.math.sqrt

object InsightEngine {

    data class BatteryInsight(
        val titleRes: Int,
        val descriptionRes: Int,
        val descriptionArgs: List<Any> = emptyList(),
        val type: InsightType = InsightType.NORMAL,
        val causeRes: Int? = null,
        val causeArgs: List<Any> = emptyList(),
        val fixRes: Int? = null
    )

    enum class InsightType {
        NORMAL, WARNING, CRITICAL, TIP, ANOMALY
    }

    fun generateInsights(list: List<BatteryEntity>, settings: AppSettings = AppSettings()): List<BatteryInsight> {
        if (list.size < 2) return emptyList()

        val insights = mutableListOf<BatteryInsight>()
        val latest = list.first()
        val oldest = list.last()

        // 0. AI Anomaly Detection (New)
        if (settings.enableAIPredictions) {
            detectAnomalies(list)?.let { insights.add(it) }
        }

        // 1. Explain My Drain
        val totalDrop = (oldest.level - latest.level).toInt()
        if (totalDrop > 3) {
            val avgTemp = list.map { it.temperature }.average()
            val screenOnCount = list.count { it.isScreenOn }
            val weakSignalCount = list.count { it.signalStrength in 0..1 }

            val causeRes = when {
                avgTemp > settings.highTempThreshold -> R.string.insight_heat_cause_background 
                screenOnCount > list.size * 0.4 -> R.string.explain_my_drain 
                weakSignalCount > list.size * 0.3 -> R.string.insight_signal_cause
                else -> null
            }

            insights.add(
                BatteryInsight(
                    titleRes = R.string.explain_my_drain,
                    descriptionRes = R.string.insight_drain_desc,
                    descriptionArgs = listOf(totalDrop),
                    causeRes = causeRes,
                    fixRes = R.string.insight_drain_fix,
                    type = if (totalDrop > 15) InsightType.WARNING else InsightType.NORMAL
                )
            )
        }

        // 2. Abnormal Drain Detection
        val drainRate = calculateDrainRate(list)
        if (drainRate > 8.0f) {
            insights.add(
                BatteryInsight(
                    titleRes = R.string.abnormal_drain,
                    descriptionRes = R.string.insight_abnormal_desc,
                    causeRes = R.string.insight_abnormal_cause,
                    causeArgs = listOf(drainRate),
                    fixRes = R.string.insight_abnormal_fix,
                    type = InsightType.CRITICAL
                )
            )
        }

        // 3. Heat Intelligence
        val highTempEntries = list.filter { it.temperature > settings.highTempThreshold }
        if (highTempEntries.isNotEmpty()) {
            val isCharging = highTempEntries.any { it.isCharging }
            insights.add(
                BatteryInsight(
                    titleRes = R.string.heat_intelligence,
                    descriptionRes = R.string.insight_heat_desc,
                    descriptionArgs = listOf(highTempEntries.maxOf { it.temperature }),
                    causeRes = if (isCharging) R.string.insight_heat_cause_charging else R.string.insight_heat_cause_background,
                    fixRes = R.string.insight_heat_fix,
                    type = InsightType.WARNING
                )
            )
        }

        // 4. Signal Impact
        val weakSignals = list.count { it.signalStrength in 0..1 }
        if (weakSignals > list.size * 0.4) {
            insights.add(
                BatteryInsight(
                    titleRes = R.string.signal_impact,
                    descriptionRes = R.string.insight_signal_desc,
                    causeRes = R.string.insight_signal_cause,
                    fixRes = R.string.insight_signal_fix,
                    type = InsightType.TIP
                )
            )
        }

        // 5. Smart Charging Coach
        if (latest.isCharging && latest.level > 80) {
            insights.add(
                BatteryInsight(
                    titleRes = R.string.charging_coach,
                    descriptionRes = R.string.insight_coach_full_desc,
                    causeRes = R.string.insight_coach_full_cause,
                    fixRes = R.string.insight_coach_full_fix,
                    type = InsightType.TIP
                )
            )
        }

        // 6. Low Battery Alert (Threshold based)
        if (!latest.isCharging && latest.level <= settings.lowBatteryThreshold) {
            insights.add(
                BatteryInsight(
                    titleRes = R.string.low_battery_warning,
                    descriptionRes = R.string.insight_low_battery_desc,
                    descriptionArgs = listOf(latest.level.toInt()),
                    type = InsightType.CRITICAL,
                    fixRes = R.string.insight_low_battery_fix
                )
            )
        }

        return insights
    }

    private fun calculateDrainRate(list: List<BatteryEntity>): Float {
        if (list.size < 2) return 0f
        val latest = list.first()
        val oldest = list.last()
        val hours = (latest.timestamp - oldest.timestamp) / (1000f * 60 * 60f)
        return if (hours > 0) (oldest.level - latest.level) / hours else 0f
    }

    private fun detectAnomalies(list: List<BatteryEntity>): BatteryInsight? {
        if (list.size < 10) return null // Need enough data for "AI" detection

        // A. Night Drain Anomaly (12 AM - 6 AM)
        val nightData = list.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour in 0..6
        }
        if (nightData.size >= 2) {
            val nightDrain = (nightData.last().level - nightData.first().level)
            val durationHours = (nightData.first().timestamp - nightData.last().timestamp) / (1000f * 60 * 60f)
            val nightRate = if (durationHours > 0) nightDrain / durationHours else 0f
            
            if (nightRate > 2.0f && !nightData.any { it.isCharging }) {
                return BatteryInsight(
                    titleRes = R.string.anomaly_detected,
                    descriptionRes = R.string.insight_night_drain_desc,
                    descriptionArgs = listOf(nightDrain.toInt()),
                    causeRes = R.string.insight_night_drain_cause,
                    fixRes = R.string.insight_night_drain_fix,
                    type = InsightType.ANOMALY
                )
            }
        }

        // B. Rapid Temperature Spike (Thermal Anomaly)
        // Check for temp increase > 2 degrees in < 5 mins without charging
        for (i in 0 until list.size - 5) {
            val later = list[i]
            val earlier = list[i + 5]
            val tempDiff = later.temperature - earlier.temperature
            val timeDiffMins = (later.timestamp - earlier.timestamp) / (1000f * 60f)
            
            if (tempDiff > 2.0f && timeDiffMins < 10f && !later.isCharging) {
                return BatteryInsight(
                    titleRes = R.string.anomaly_detected,
                    descriptionRes = R.string.insight_rapid_temp_desc,
                    descriptionArgs = listOf(tempDiff),
                    causeRes = R.string.insight_rapid_temp_cause,
                    fixRes = R.string.insight_rapid_temp_fix,
                    type = InsightType.ANOMALY
                )
            }
        }

        // C. Statistical Drain Anomaly (Z-Score)
        val rates = mutableListOf<Float>()
        // Calculate rates over 15-minute segments to reduce noise
        for (i in 0 until list.size - 1) {
            val later = list[i]
            val earlier = list[i+1]
            val h = (later.timestamp - earlier.timestamp) / (1000f * 60 * 60f)
            // Ignore very small time differences to avoid infinite rates
            if (h > 0.05) rates.add((earlier.level - later.level) / h)
        }
        
        if (rates.size >= 5) {
            val avg = rates.average()
            val stdDev = sqrt(rates.map { (it - avg).pow(2.0) }.average())
            val currentRate = rates.first()
            
            if (stdDev > 0.5 && (currentRate - avg) / stdDev > 2.0) { // 2.0 Sigma is usually enough for battery
                return BatteryInsight(
                    titleRes = R.string.anomaly_detected,
                    descriptionRes = R.string.insight_abnormal_desc,
                    causeRes = R.string.insight_abnormal_cause,
                    causeArgs = listOf("%.1f".format(currentRate)),
                    fixRes = R.string.insight_abnormal_fix,
                    type = InsightType.ANOMALY
                )
            }
        }

        return null
    }
}
