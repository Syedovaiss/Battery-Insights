package com.ovais.batterymonitorer.feature.home.data

import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun observeBattery(): Flow<List<BatteryEntity>>
    suspend fun insert(entity: BatteryEntity)
    fun observeRecentReports(): Flow<List<DailyReportEntity>>
    suspend fun insertReport(report: DailyReportEntity)
    suspend fun getAllOnce(): List<BatteryEntity>
    suspend fun getAllReportsOnce(): List<DailyReportEntity>
    suspend fun purgeHistoryOlderThan(retentionDays: Int)
    suspend fun refreshBatteryState()
}