package com.ovais.batterymonitorer.storage.di

import android.content.Context
import androidx.room.Room
import com.ovais.batterymonitorer.storage.database.dao.BatteryDao
import com.ovais.batterymonitorer.storage.database.dao.DailyReportDao
import com.ovais.batterymonitorer.storage.database.db.BatteryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BatteryDatabase {
        return Room.databaseBuilder(
            context,
            BatteryDatabase::class.java,
            "battery_db"
        )
            .addMigrations(BatteryDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideDao(db: BatteryDatabase): BatteryDao = db.batteryDao()

    @Provides
    fun provideDailyReportDao(db: BatteryDatabase): DailyReportDao = db.dailyReportDao()
}