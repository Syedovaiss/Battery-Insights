package com.ovais.batterymonitorer.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ovais.batterymonitorer.storage.database.entity.BatteryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BatteryEntity)

    @Query("SELECT * FROM battery_data ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BatteryEntity>>

    @Query("SELECT * FROM battery_data")
    suspend fun getAllOnce(): List<BatteryEntity>

    @Query("DELETE FROM battery_data WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}