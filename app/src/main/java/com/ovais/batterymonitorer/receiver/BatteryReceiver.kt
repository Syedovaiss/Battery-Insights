package com.ovais.batterymonitorer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ovais.batterymonitorer.feature.home.domain.BatteryTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BatteryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var batteryTracker: BatteryTracker

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> batteryTracker.trackBatteryChange(intent)
                    Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> batteryTracker.generateDailyReport()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
