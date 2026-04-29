package com.ovais.batterymonitorer.storage.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ovais.batterymonitorer.storage.database.dao.BatteryDao
import com.ovais.batterymonitorer.storage.database.dao.DailyReportDao
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity

@Database(
    entities = [BatteryEntity::class, DailyReportEntity::class],
    version = 3,
    exportSchema = true
)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
    abstract fun dailyReportDao(): DailyReportDao
}