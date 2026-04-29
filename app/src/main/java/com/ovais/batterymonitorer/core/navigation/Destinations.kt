package com.ovais.batterymonitorer.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    data object Home : Destination
    @Serializable
    data object Settings : Destination
}
