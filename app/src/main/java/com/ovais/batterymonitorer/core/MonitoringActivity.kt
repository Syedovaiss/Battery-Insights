package com.ovais.batterymonitorer.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ovais.batterymonitorer.core.navigation.MonitoringNavigation
import com.ovais.batterymonitorer.core.ui.theme.BatteryMonitorerTheme
import com.ovais.batterymonitorer.feature.home.domain.BatteryTracker
import com.ovais.batterymonitorer.feature.settings.data.SettingsRepository
import com.ovais.batterymonitorer.receiver.BatteryReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringActivity : ComponentActivity() {

    @Inject
    lateinit var batteryTracker: BatteryTracker

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = null).value
            BatteryMonitorerTheme(
                darkTheme = settings?.darkThemeEnabled ?: true,
                dynamicColor = settings?.dynamicColorsEnabled ?: true
            ) {
                val context = LocalContext.current
                DisposableEffect(Unit) {
                    val receiver = object : android.content.BroadcastReceiver() {
                        override fun onReceive(ctx: Context?, intent: Intent?) {
                            if (intent != null) {
                                lifecycleScope.launch {
                                    batteryTracker.trackBatteryChange(intent)
                                }
                            }
                        }
                    }
                    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    context.registerReceiver(receiver, filter)
                    onDispose {
                        context.unregisterReceiver(receiver)
                    }
                }

                val permissions = mutableListOf<String>().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    add(Manifest.permission.READ_PHONE_STATE)
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    // Handle results if needed
                }

                LaunchedEffect(Unit) {
                    val toRequest = permissions.filter {
                        ContextCompat.checkSelfPermission(
                            this@MonitoringActivity,
                            it
                        ) != PackageManager.PERMISSION_GRANTED
                    }
                    if (toRequest.isNotEmpty()) {
                        launcher.launch(toRequest.toTypedArray())
                    }
                }


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MonitoringNavigation(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
