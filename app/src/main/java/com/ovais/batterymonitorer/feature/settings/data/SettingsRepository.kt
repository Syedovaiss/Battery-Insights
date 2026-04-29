package com.ovais.batterymonitorer.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ovais.batterymonitorer.feature.settings.domain.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val HIGH_TEMP_THRESHOLD = floatPreferencesKey("high_temp_threshold")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val POLLING_INTERVAL = intPreferencesKey("polling_interval")
        val ENABLE_AI = booleanPreferencesKey("enable_ai")
        val HISTORY_POINTS = intPreferencesKey("history_points")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val CHARGE_LIMIT_ENABLED = booleanPreferencesKey("charge_limit_enabled")
        val CHARGE_LIMIT_PERCENT = intPreferencesKey("charge_limit_percent")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            lowBatteryThreshold = prefs[Keys.LOW_BATTERY_THRESHOLD] ?: 20,
            highTempThreshold = prefs[Keys.HIGH_TEMP_THRESHOLD] ?: 40f,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            accentColor = prefs[Keys.ACCENT_COLOR] ?: 0xFF00FFFF,
            pollingIntervalMinutes = prefs[Keys.POLLING_INTERVAL] ?: 15,
            enableAIPredictions = prefs[Keys.ENABLE_AI] ?: true,
            historyPointsCount = prefs[Keys.HISTORY_POINTS] ?: 24,
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: true,
            dynamicColorsEnabled = prefs[Keys.DYNAMIC_COLORS] ?: true,
            chargeLimitEnabled = prefs[Keys.CHARGE_LIMIT_ENABLED] ?: true,
            chargeLimitPercent = prefs[Keys.CHARGE_LIMIT_PERCENT] ?: 80
        )
    }

    suspend fun updateLowBatteryThreshold(value: Int) {
        context.dataStore.edit { it[Keys.LOW_BATTERY_THRESHOLD] = value.coerceIn(5, 40) }
    }

    suspend fun updateHighTempThreshold(value: Float) {
        context.dataStore.edit { it[Keys.HIGH_TEMP_THRESHOLD] = value.coerceIn(30f, 50f) }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateAccentColor(color: Long) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }
    }

    suspend fun updatePollingInterval(minutes: Int) {
        context.dataStore.edit { it[Keys.POLLING_INTERVAL] = minutes.coerceIn(15, 60) }
    }

    suspend fun updateAIEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ENABLE_AI] = enabled }
    }

    suspend fun updateHistoryPointsCount(count: Int) {
        context.dataStore.edit { it[Keys.HISTORY_POINTS] = count.coerceIn(12, 48) }
    }

    suspend fun updateDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun updateDynamicColorsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun updateChargeLimitEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CHARGE_LIMIT_ENABLED] = enabled }
    }

    suspend fun updateChargeLimitPercent(percent: Int) {
        context.dataStore.edit { it[Keys.CHARGE_LIMIT_PERCENT] = percent.coerceIn(70, 95) }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit {
            it[Keys.LOW_BATTERY_THRESHOLD] = 20
            it[Keys.HIGH_TEMP_THRESHOLD] = 40f
            it[Keys.NOTIFICATIONS_ENABLED] = true
            it[Keys.ACCENT_COLOR] = 0xFF00FFFF
            it[Keys.POLLING_INTERVAL] = 15
            it[Keys.ENABLE_AI] = true
            it[Keys.HISTORY_POINTS] = 24
            it[Keys.DARK_THEME] = true
            it[Keys.DYNAMIC_COLORS] = true
            it[Keys.CHARGE_LIMIT_ENABLED] = true
            it[Keys.CHARGE_LIMIT_PERCENT] = 80
        }
    }
}
