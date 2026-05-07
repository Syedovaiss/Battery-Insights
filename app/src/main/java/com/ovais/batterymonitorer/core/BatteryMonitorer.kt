package com.ovais.batterymonitorer.core

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ovais.batterymonitorer.core.telemetry.AppContextHolder
import com.ovais.batterymonitorer.core.telemetry.AppTelemetry
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class BatteryMonitorer : Application(), Configuration.Provider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkManagerEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(EntryPoints.get(this, WorkManagerEntryPoint::class.java).workerFactory())
            .build()

    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        AppTelemetry.breadcrumb("app_start")
    }
}
