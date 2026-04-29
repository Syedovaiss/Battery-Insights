package com.ovais.batterymonitorer.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovais.batterymonitorer.feature.home.domain.BatteryTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BatteryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val batteryTracker: BatteryTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val intent = applicationContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        intent?.let {
            batteryTracker.trackBatteryChange(it)
        }

        return Result.success()
    }
}
