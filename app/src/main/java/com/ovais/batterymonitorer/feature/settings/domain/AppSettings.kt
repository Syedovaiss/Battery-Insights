package com.ovais.batterymonitorer.feature.settings.domain

data class AppSettings(
    val lowBatteryThreshold: Int = 20,
    val highTempThreshold: Float = 40f,
    val notificationsEnabled: Boolean = true,
    val accentColor: Long = 0xFF00FFFF, // Cyan
    val pollingIntervalMinutes: Int = 15,
    val enableAIPredictions: Boolean = true,
    val historyPointsCount: Int = 24,
    val darkThemeEnabled: Boolean = true,
    val dynamicColorsEnabled: Boolean = true,
    val chargeLimitEnabled: Boolean = true,
    val chargeLimitPercent: Int = 80
)
