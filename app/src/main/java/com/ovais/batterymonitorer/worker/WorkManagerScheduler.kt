package com.ovais.batterymonitorer.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleBatteryPolling(intervalMinutes: Int) {
        val request = PeriodicWorkRequestBuilder<BatteryWorker>(
            intervalMinutes.toLong().coerceAtLeast(15L), // WorkManager minimum is 15 mins
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "BatteryPolling",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
