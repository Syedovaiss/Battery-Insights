package com.ovais.batterymonitorer.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reports")
data class DailyReportEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val avgTemp: Float,
    val totalDrain: Float,
    val avgSignalStrength: Float,
    val screenOnTimeMinutes: Long,
    val screenOffDrain: Float,
    val screenOnDrain: Float
)
