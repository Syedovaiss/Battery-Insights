package com.ovais.batterymonitorer.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ovais.batterymonitorer.R

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    val accentColor = Color(settings.accentColor.toInt())
    var showExportDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_personalization))
            }

            item {
                AccentColorPicker(
                    selectedColor = accentColor,
                    onColorSelected = { viewModel.updateAccentColor(it.toArgb().toLong()) }
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_alerts_thresholds))
            }

            item {
                SliderSettingCard(
                    title = stringResource(R.string.settings_low_battery_alert),
                    value = settings.lowBatteryThreshold.toFloat(),
                    valueRange = 5f..40f,
                    unit = "%",
                    icon = Icons.Default.BatteryAlert,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_low_battery_alert_info),
                    onValueChange = { viewModel.updateLowBatteryThreshold(it.toInt()) }
                )
            }

            item {
                SliderSettingCard(
                    title = stringResource(R.string.settings_high_temp_alert),
                    value = settings.highTempThreshold,
                    valueRange = 30f..50f,
                    unit = "°C",
                    icon = Icons.Default.Thermostat,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_high_temp_alert_info),
                    onValueChange = { viewModel.updateHighTempThreshold(it) }
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_system))
            }

            item {
                ToggleSettingCard(
                    title = stringResource(R.string.settings_push_notifications),
                    description = stringResource(R.string.settings_push_notifications_desc),
                    checked = settings.notificationsEnabled,
                    icon = Icons.Default.Notifications,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_push_notifications_info),
                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    title = stringResource(R.string.settings_ai_predictions),
                    description = stringResource(R.string.settings_ai_predictions_desc),
                    checked = settings.enableAIPredictions,
                    icon = Icons.Default.AutoAwesome,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_ai_predictions_info),
                    onCheckedChange = { viewModel.updateAIEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    title = stringResource(R.string.settings_dark_theme),
                    description = stringResource(R.string.settings_dark_theme_desc),
                    checked = settings.darkThemeEnabled,
                    icon = Icons.Default.DarkMode,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_dark_theme_info),
                    onCheckedChange = { viewModel.updateDarkThemeEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    title = stringResource(R.string.settings_dynamic_colors),
                    description = stringResource(R.string.settings_dynamic_colors_desc),
                    checked = settings.dynamicColorsEnabled,
                    icon = Icons.Default.ColorLens,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_dynamic_colors_info),
                    onCheckedChange = { viewModel.updateDynamicColorsEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    title = stringResource(R.string.settings_charge_limit_alert),
                    description = stringResource(R.string.settings_charge_limit_alert_desc),
                    checked = settings.chargeLimitEnabled,
                    icon = Icons.Default.Bolt,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_charge_limit_alert_info),
                    onCheckedChange = { viewModel.updateChargeLimitEnabled(it) }
                )
            }

            item {
                SliderSettingCard(
                    title = stringResource(R.string.settings_charge_limit_percent),
                    value = settings.chargeLimitPercent.toFloat(),
                    valueRange = 70f..95f,
                    steps = 4,
                    unit = "%",
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_charge_limit_percent_info),
                    onValueChange = { viewModel.updateChargeLimitPercent(it.toInt()) }
                )
            }

            item {
                SliderSettingCard(
                    title = stringResource(R.string.settings_polling_interval),
                    value = settings.pollingIntervalMinutes.toFloat(),
                    valueRange = 15f..60f,
                    steps = 3,
                    unit = " min",
                    icon = Icons.Default.Update,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_polling_interval_info),
                    onValueChange = { viewModel.updatePollingInterval(it.toInt()) }
                )
            }

            item {
                SliderSettingCard(
                    title = stringResource(R.string.settings_history_view),
                    value = settings.historyPointsCount.toFloat(),
                    valueRange = 12f..48f,
                    steps = 3,
                    unit = " pts",
                    icon = Icons.Default.History,
                    accentColor = accentColor,
                    infoText = stringResource(R.string.settings_history_view_info),
                    onValueChange = { viewModel.updateHistoryPointsCount(it.toInt()) }
                )
            }

            item {
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.12f),
                        contentColor = accentColor
                    ),
                    enabled = exportStatus != SettingsExportStatus.LOADING
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when (exportStatus) {
                            SettingsExportStatus.LOADING -> stringResource(R.string.export_in_progress)
                            SettingsExportStatus.SUCCESS -> stringResource(R.string.export_done)
                            SettingsExportStatus.ERROR -> stringResource(R.string.export_failed)
                            SettingsExportStatus.IDLE -> stringResource(R.string.settings_export_data)
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_reset))
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_format_title)) },
            text = { Text(stringResource(R.string.export_format_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.exportData(ExportFormat.CSV)
                    showExportDialog = false
                }) {
                    Text(stringResource(R.string.export_format_csv))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.exportData(ExportFormat.JSON)
                        showExportDialog = false
                    }) {
                        Text(stringResource(R.string.export_format_json))
                    }
                    TextButton(onClick = {
                        viewModel.exportData(ExportFormat.PDF)
                        showExportDialog = false
                    }) {
                        Text(stringResource(R.string.export_format_pdf))
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun AccentColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF00FFFF), // Cyan
        Color(0xFFFF00FF), // Magenta
        Color(0xFF7C4DFF), // Deep Purple
        Color(0xFF00E676), // Green
        Color(0xFFFF5252), // Red
        Color(0xFFFFD740)  // Amber
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = selectedColor)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.settings_accent_color),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 3.dp,
                                color = if (selectedColor == color) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

@Composable
fun SliderSettingCard(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    infoText: String? = null,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = accentColor.copy(alpha = 0.6f))
                    Spacer(Modifier.width(12.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (!infoText.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        SettingInfoTooltip(infoText = infoText)
                    }
                }
                Text(
                    text = "${value.toInt()}$unit",
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    fontSize = 18.sp
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun ToggleSettingCard(
    title: String,
    description: String,
    checked: Boolean,
    icon: ImageVector,
    accentColor: Color,
    infoText: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (!infoText.isNullOrBlank()) {
                            Spacer(Modifier.width(6.dp))
                            SettingInfoTooltip(infoText = infoText)
                        }
                    }
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accentColor,
                    checkedTrackColor = accentColor.copy(alpha = 0.4f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    uncheckedTrackColor = Color.Transparent,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun SettingInfoTooltip(infoText: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.settings_info),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(infoText, style = MaterialTheme.typography.bodySmall) },
                onClick = { expanded = false }
            )
        }
    }
}
