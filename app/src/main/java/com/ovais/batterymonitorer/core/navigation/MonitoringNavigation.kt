package com.ovais.batterymonitorer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.ovais.batterymonitorer.feature.home.presentation.BatteryScreen
import com.ovais.batterymonitorer.feature.settings.presentation.SettingsScreen


@Composable
fun MonitoringNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberSaveable(
        saver = listSaver(
            save = { destinations ->
                destinations.map { destination ->
                    when (destination) {
                        Destination.Home -> "home"
                        Destination.Settings -> "settings"
                    }
                }
            },
            restore = {
                mutableStateListOf<Destination>().apply {
                    addAll(
                        it.mapNotNull { route ->
                            when (route) {
                                "home" -> Destination.Home
                                "settings" -> Destination.Settings
                                else -> null
                            }
                        }
                    )
                }
            }
        )
    ) {
        mutableStateListOf<Destination>(Destination.Home)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { key ->
            when (key) {
                is Destination.Home -> NavEntry(key) {
                    BatteryScreen(
                        onNavigateToSettings = {
                            backStack.add(Destination.Settings)
                        }
                    )
                }

                is Destination.Settings -> NavEntry(key) {
                    SettingsScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    )
}