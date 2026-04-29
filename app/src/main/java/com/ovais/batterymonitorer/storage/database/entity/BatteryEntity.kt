package com.ovais.batterymonitorer.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_data")
data class BatteryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val level: Float,
    val temperature: Float,
    val isCharging: Boolean,
    val isScreenOn: Boolean = false,
    val signalStrength: Int = -1 // -1 if unknown, otherwise 0-4 or dBM
)