package com.ovais.batterymonitorer.feature.home.domain

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import com.ovais.batterymonitorer.R
import com.ovais.batterymonitorer.feature.engine.InsightEngine
import com.ovais.batterymonitorer.feature.home.data.BatteryRepository
import com.ovais.batterymonitorer.feature.settings.data.SettingsRepository
import com.ovais.batterymonitorer.core.telemetry.AppTelemetry
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity
import com.ovais.batterymonitorer.utils.showNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryTracker @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: BatteryRepository,
    private val settingsRepository: SettingsRepository
) {
    private var hasNotifiedChargeLimitForCurrentSession: Boolean = false
    private var lastHistoryCleanupAtMs: Long = 0L
    private var lastGeneralInsightAtMs: Long = 0L
    private var lastGeneralInsightKey: String? = null
    private var lastLowBatteryAlertAtMs: Long = 0L
    private var lastLowBatteryAlertLevel: Int = 101

    private companion object {
        const val GENERAL_INSIGHT_COOLDOWN_MS = 20 * 60 * 1000L
        const val LOW_BATTERY_COOLDOWN_MS = 25 * 60 * 1000L
        const val LOW_BATTERY_RENOTIFY_DROP_PERCENT = 3
        const val HISTORY_CLEANUP_INTERVAL_MS = 12 * 60 * 60 * 1000L
    }

    suspend fun trackBatteryChange(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val signalStrength = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            telephonyManager.signalStrength?.level ?: -1
        } else {
            -1
        }

        val percent = if (level != -1 && scale != -1) (level * 100f / scale) else 0f

        val entity = BatteryEntity(
            timestamp = System.currentTimeMillis(),
            level = percent,
            temperature = temp,
            isCharging = isCharging,
            isScreenOn = isScreenOn,
            signalStrength = signalStrength
        )
        repository.insert(entity)

        val settings = settingsRepository.settingsFlow.first()
        maybeCleanupHistory(settings.autoDeleteHistoryDays)
        // Charge-limit alert (AccuBattery-like)
        if (settings.notificationsEnabled && settings.chargeLimitEnabled) {
            if (isCharging && percent >= settings.chargeLimitPercent) {
                if (!hasNotifiedChargeLimitForCurrentSession) {
                    showNotification(
                        context,
                        "Charge reached ${settings.chargeLimitPercent}%. Unplug now to reduce battery wear."
                    )
                    AppTelemetry.breadcrumb(
                        "charge_limit_alert",
                        mapOf("limit" to settings.chargeLimitPercent.toString())
                    )
                    hasNotifiedChargeLimitForCurrentSession = true
                }
            } else if (!isCharging || percent < (settings.chargeLimitPercent - 3)) {
                hasNotifiedChargeLimitForCurrentSession = false
            }
        }

        // Check for critical insights and notify
        if (settings.notificationsEnabled) {
            val history = repository.observeBattery().first().take(20)
            val insights = InsightEngine.generateInsights(history, settings)

            insights.find { 
                it.type == InsightEngine.InsightType.CRITICAL || 
                it.type == InsightEngine.InsightType.WARNING || 
                it.type == InsightEngine.InsightType.ANOMALY 
            }?.let {
                val title = context.getString(it.titleRes)
                val desc = context.getString(it.descriptionRes, *it.descriptionArgs.toTypedArray())
                val now = System.currentTimeMillis()
                val shouldNotify = if (it.titleRes == R.string.low_battery_warning) {
                    shouldNotifyLowBattery(
                        currentLevel = percent.toInt(),
                        isCharging = isCharging,
                        nowMs = now
                    )
                } else {
                    shouldNotifyGeneralInsight(
                        insightKey = "${it.titleRes}:${it.descriptionRes}",
                        nowMs = now
                    )
                }
                if (shouldNotify) {
                    showNotification(context, "$title: $desc")
                    AppTelemetry.breadcrumb(
                        "insight_notification",
                        mapOf("title_res" to it.titleRes.toString())
                    )
                }
            }
        }
    }

    private suspend fun maybeCleanupHistory(retentionDays: Int) {
        val now = System.currentTimeMillis()
        if (now - lastHistoryCleanupAtMs < HISTORY_CLEANUP_INTERVAL_MS) return
        repository.purgeHistoryOlderThan(retentionDays)
        lastHistoryCleanupAtMs = now
        AppTelemetry.breadcrumb("history_cleanup", mapOf("retention_days" to retentionDays.toString()))
    }

    private fun shouldNotifyGeneralInsight(insightKey: String, nowMs: Long): Boolean {
        val withinCooldown = nowMs - lastGeneralInsightAtMs < GENERAL_INSIGHT_COOLDOWN_MS
        val isSameAsLast = insightKey == lastGeneralInsightKey
        if (withinCooldown && isSameAsLast) return false

        lastGeneralInsightAtMs = nowMs
        lastGeneralInsightKey = insightKey
        return true
    }

    private fun shouldNotifyLowBattery(currentLevel: Int, isCharging: Boolean, nowMs: Long): Boolean {
        if (isCharging) {
            lastLowBatteryAlertLevel = 101
            lastLowBatteryAlertAtMs = 0L
            return false
        }

        val withinCooldown = nowMs - lastLowBatteryAlertAtMs < LOW_BATTERY_COOLDOWN_MS
        val droppedEnoughSinceLast = currentLevel <= (lastLowBatteryAlertLevel - LOW_BATTERY_RENOTIFY_DROP_PERCENT)

        if (withinCooldown && !droppedEnoughSinceLast) return false
        if (!droppedEnoughSinceLast && lastLowBatteryAlertAtMs != 0L) return false

        lastLowBatteryAlertAtMs = nowMs
        lastLowBatteryAlertLevel = currentLevel
        return true
    }

    suspend fun generateDailyReport() {
        val allData = repository.getAllOnce()
        if (allData.isEmpty()) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000))
        
        val yesterdayData = allData.filter { sdf.format(Date(it.timestamp)) == yesterday }
        if (yesterdayData.isEmpty()) return

        val avgTemp = yesterdayData.map { it.temperature }.average().toFloat()
        val avgSignal = yesterdayData.map { it.signalStrength.toFloat() }.filter { it >= 0 }.average().toFloat()
        
        val sorted = yesterdayData.sortedBy { it.timestamp }
        val totalDrain = (sorted.first().level - sorted.last().level).coerceAtLeast(0f)
        
        // Basic SOT calculation
        var sotMillis = 0L
        for (i in 0 until sorted.size - 1) {
            if (sorted[i].isScreenOn) {
                sotMillis += (sorted[i+1].timestamp - sorted[i].timestamp)
            }
        }

        val report = DailyReportEntity(
            date = yesterday,
            avgTemp = avgTemp,
            totalDrain = totalDrain,
            avgSignalStrength = avgSignal,
            screenOnTimeMinutes = sotMillis / 60000,
            screenOffDrain = 0f, 
            screenOnDrain = 0f
        )
        repository.insertReport(report)
    }
}
