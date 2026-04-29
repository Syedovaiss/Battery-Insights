package com.ovais.batterymonitorer.feature.home.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import com.ovais.batterymonitorer.feature.home.data.BatteryRepository
import com.ovais.batterymonitorer.storage.database.dao.BatteryDao
import com.ovais.batterymonitorer.storage.database.dao.DailyReportDao
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultBatteryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: BatteryDao,
    private val reportDao: DailyReportDao
) : BatteryRepository {

    override fun observeBattery(): Flow<List<BatteryEntity>> = dao.observeAll()

    override suspend fun insert(entity: BatteryEntity) {
        dao.insert(entity)
    }

    override fun observeRecentReports(): Flow<List<DailyReportEntity>> = reportDao.observeRecentReports()

    override suspend fun insertReport(report: DailyReportEntity) {
        reportDao.insertReport(report)
    }

    override suspend fun getAllOnce(): List<BatteryEntity> = dao.getAllOnce()

    override suspend fun getAllReportsOnce(): List<DailyReportEntity> = reportDao.getAllReports()

    override suspend fun refreshBatteryState() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100f /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)).takeIf { it >= 0 }
                ?: (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
                    .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()

            val tempValue = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
            val statusValue = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isChargingValue = statusValue == BatteryManager.BATTERY_STATUS_CHARGING ||
                    statusValue == BatteryManager.BATTERY_STATUS_FULL

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOnValue = powerManager.isInteractive

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val signalStrengthValue = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                telephonyManager.signalStrength?.level ?: -1
            } else {
                -1
            }

            dao.insert(
                BatteryEntity(
                    timestamp = System.currentTimeMillis(),
                    level = batteryLevel,
                    temperature = tempValue,
                    isCharging = isChargingValue,
                    isScreenOn = isScreenOnValue,
                    signalStrength = signalStrengthValue
                )
            )
        }
    }
}