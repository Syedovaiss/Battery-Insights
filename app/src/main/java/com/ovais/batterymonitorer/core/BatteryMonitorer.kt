package com.ovais.batterymonitorer.core

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ovais.batterymonitorer.core.telemetry.AppContextHolder
import com.ovais.batterymonitorer.core.telemetry.AppTelemetry
import com.ovais.batterymonitorer.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BatteryMonitorer : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var scheduler: WorkManagerScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        AppTelemetry.breadcrumb("app_start")
        scheduler.scheduleBatteryPolling(15)
    }
}
