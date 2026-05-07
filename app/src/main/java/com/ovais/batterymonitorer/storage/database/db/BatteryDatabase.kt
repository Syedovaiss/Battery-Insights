package com.ovais.batterymonitorer.storage.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ovais.batterymonitorer.storage.database.dao.BatteryDao
import com.ovais.batterymonitorer.storage.database.dao.DailyReportDao
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity

@Database(
    entities = [BatteryEntity::class, DailyReportEntity::class],
    version = 4,
    exportSchema = true
)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
    abstract fun dailyReportDao(): DailyReportDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE battery_data ADD COLUMN voltage INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE battery_data ADD COLUMN currentNow INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}