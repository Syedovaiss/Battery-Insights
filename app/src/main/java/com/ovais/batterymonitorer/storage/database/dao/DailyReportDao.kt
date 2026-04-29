package com.ovais.batterymonitorer.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: DailyReportEntity)

    @Query("SELECT * FROM daily_reports ORDER BY date DESC LIMIT 14")
    fun observeRecentReports(): Flow<List<DailyReportEntity>>

    @Query("SELECT * FROM daily_reports WHERE date = :date")
    suspend fun getReportForDate(date: String): DailyReportEntity?

    @Query("SELECT * FROM daily_reports ORDER BY date DESC")
    suspend fun getAllReports(): List<DailyReportEntity>

    @Query("DELETE FROM daily_reports WHERE date < :cutoffDate")
    suspend fun deleteOlderThanDate(cutoffDate: String)
}
