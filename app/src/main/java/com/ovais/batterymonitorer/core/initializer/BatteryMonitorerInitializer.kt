package com.ovais.batterymonitorer.core.initializer

import android.content.Context
import androidx.startup.Initializer
import timber.log.Timber

class BatteryMonitorerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // Initialize Timber
        Timber.plant(Timber.DebugTree())
        Timber.d("BatteryMonitorerInitializer: Initializing...")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
