package com.ovais.batterymonitorer.feature.home.presentation

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ovais.batterymonitorer.R
import com.ovais.batterymonitorer.feature.engine.InsightEngine
import com.ovais.batterymonitorer.storage.database.entity.DailyReportEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BatteryScreen(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    viewModel: BatteryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val accentColor = Color(state.settings.accentColor.toInt())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = accentColor)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onNavigateToSettings() }
                        )
                    }
                }

                item {
                    MainDashboardCard(state, accentColor)
                }

                item {
                    BatteryHealthCard(
                        healthScore = state.batteryHealthScore,
                        accentColor = accentColor
                    )
                }

                if (state.isCharging) {
                    item {
                        ChargingSessionCard(
                            minutes = state.activeChargeSessionMinutes,
                            gainPercent = state.activeChargeSessionGainPercent,
                            speedPerHour = state.activeChargeSpeedPercentPerHour,
                            accentColor = accentColor
                        )
                    }
                }

                item {
                    EcoAdviceCard(
                        advice = if (state.ecoAdviceRes != 0) {
                            stringResource(state.ecoAdviceRes, *state.ecoAdviceArgs.toTypedArray())
                        } else null,
                        accentColor = accentColor
                    )
                }

                item {
                    BatteryGraph(state.history, accentColor)
                }

                item {
                    AppBatteryUsageCard(
                        accentColor = accentColor,
                        onOpenSystemBatteryUsage = {
                            openBatteryUsageSettings(context)
                        }
                    )
                }

                item {
                    SimulatorCard(
                        screenTime = state.simulatorScreenTimeHours,
                        locationOn = state.simulatorLocationOn,
                        brightness = state.simulatorBrightness,
                        is5G = state.simulatorNetwork5G,
                        savings = state.simulatedSavings,
                        onScreenTimeChange = { viewModel.process(BatteryIntent.UpdateSimulatorScreenTime(it)) },
                        onLocationChange = { viewModel.process(BatteryIntent.UpdateSimulatorLocation(it)) },
                        onBrightnessChange = { viewModel.process(BatteryIntent.UpdateSimulatorBrightness(it)) },
                        onNetworkChange = { viewModel.process(BatteryIntent.UpdateSimulatorNetwork(it)) },
                        accentColor = accentColor
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.battery_insights),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                }

                items(state.insights) { insight ->
                    InsightCard(insight, accentColor)
                }

                if (state.recentReports.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.battery_reports),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.reportFilter == ReportFilter.DAILY,
                                    onClick = { viewModel.process(BatteryIntent.ChangeReportFilter(ReportFilter.DAILY)) },
                                    label = { Text(stringResource(R.string.report_daily)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                        selectedLabelColor = accentColor
                                    )
                                )
                                FilterChip(
                                    selected = state.reportFilter == ReportFilter.WEEKLY,
                                    onClick = { viewModel.process(BatteryIntent.ChangeReportFilter(ReportFilter.WEEKLY)) },
                                    label = { Text(stringResource(R.string.report_weekly)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                        selectedLabelColor = accentColor
                                    )
                                )
                            }
                        }
                    }

                    item {
                        if (state.reportFilter == ReportFilter.DAILY) {
                            WeeklyReportCard(state.recentReports, accentColor)
                        } else {
                            WeeklyAggregatedReportCard(state.recentReports, accentColor)
                        }
                    }
                }
                
                item {
                    if (state.healthPredictionRes != 0) {
                        HealthPredictionCard(
                            stringResource(state.healthPredictionRes, *state.healthPredictionArgs.toTypedArray()),
                        )
                    }
                }
                
                item {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun BatteryHealthCard(
    healthScore: Int,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.battery_health_score),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.battery_health_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Text(
                text = "$healthScore%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = if (healthScore >= 85) accentColor else Color(0xFFFFAB40)
            )
        }
    }
}

@Composable
fun ChargingSessionCard(
    minutes: Int,
    gainPercent: Int,
    speedPerHour: Float,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.charge_session_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SessionMetric(
                    label = stringResource(R.string.charge_session_duration),
                    value = "${minutes}m",
                    accentColor = accentColor
                )
                SessionMetric(
                    label = stringResource(R.string.charge_session_gain),
                    value = "+${gainPercent}%",
                    accentColor = accentColor
                )
                SessionMetric(
                    label = stringResource(R.string.charge_session_speed),
                    value = String.format(Locale.US, "%.1f%%/h", speedPerHour),
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun SessionMetric(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun MainDashboardCard(state: BatteryState, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${state.currentLevel}%",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = stringResource(R.string.current_charge).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = if (state.isCharging) Color.Yellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    val drainText = String.format(Locale.US, "%.1f%%/hr", state.drainRate)
                    Text(
                        text = drainText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.drainRate > 5) Color(0xFFFF5252) else Color(0xFF4CAF50)
                    )
                    Text(
                        text = stringResource(R.string.drain_rate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }

            if (state.timeToEmptyMinutes > 0 || state.timeToFullMinutes > 0) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(16.dp))
                
                val timeMinutes = if (state.isCharging) state.timeToFullMinutes else state.timeToEmptyMinutes
                val hours = timeMinutes / 60
                val mins = timeMinutes % 60
                val res = if (state.isCharging) R.string.time_to_full else R.string.time_to_empty

                Text(
                    text = stringResource(res, hours, mins),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EcoAdviceCard(advice: String?, accentColor: Color) {
    if (advice == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.eco_advice_title).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = advice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun BatteryGraph(history: List<Float>, accentColor: Color) {
    val density = LocalDensity.current
    val animationProgress by animateFloatAsState(
        targetValue = if (history.isEmpty()) 0f else 1f,
        animationSpec = tween(durationMillis = 900),
        label = "usage_history_line_reveal"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.padding(start = 40.dp, top = 16.dp, end = 20.dp, bottom = 24.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (history.isEmpty()) return@Canvas
                
                val path = Path()
                val width = size.width
                val height = size.height
                if (history.size == 1) {
                    drawCircle(
                        color = accentColor,
                        radius = with(density) { 4.dp.toPx() },
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * (1 - history.first()))
                    )
                    return@Canvas
                }
                val stepX = width / (history.size - 1)
                
                path.moveTo(0f, height * (1 - history[0]))
                history.forEachIndexed { i, p ->
                    if (i > 0) {
                        path.lineTo(i * stepX, height * (1 - p))
                    }
                }

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(listOf(accentColor, Color.Transparent)),
                    style = Stroke(width = with(density) { 4.dp.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = 0.9f
                )

                clipRect(right = size.width * animationProgress) {
                    drawPath(
                        path = path,
                        color = accentColor.copy(alpha = 0.2f),
                        style = Stroke(width = with(density) { 12.dp.toPx() }, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(width = with(density) { 3.dp.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-34).dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(40.dp))
                Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(40.dp))
                Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.usage_history_oldest), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(stringResource(R.string.usage_history_latest), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (history.isEmpty()) {
                Text(
                    text = stringResource(R.string.usage_history_collecting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun AppBatteryUsageCard(
    accentColor: Color,
    onOpenSystemBatteryUsage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.app_battery_usage_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_battery_usage_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpenSystemBatteryUsage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor
                )
            ) {
                Text(stringResource(R.string.open_system_battery_usage))
            }
        }
    }
}

private fun openBatteryUsageSettings(context: android.content.Context) {
    val intents = listOf(
        Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
        Intent("android.intent.action.POWER_USAGE_SUMMARY"),
        Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
        Intent(Settings.ACTION_SETTINGS)
    ).map { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    val launched = intents.any { intent ->
        runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    if (!launched) {
        Toast.makeText(
            context,
            "Could not open battery settings on this device.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun SimulatorCard(
    screenTime: Float,
    locationOn: Boolean,
    brightness: Float,
    is5G: Boolean,
    savings: Int,
    onScreenTimeChange: (Float) -> Unit,
    onLocationChange: (Boolean) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onNetworkChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.what_if_simulator),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Screen Time
            Text(
                text = stringResource(R.string.daily_screen_time, screenTime.toInt()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = screenTime,
                onValueChange = onScreenTimeChange,
                valueRange = 1f..12f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )

            // Brightness
            Text(
                text = stringResource(R.string.brightness_level, brightness.toInt()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            
            Spacer(Modifier.height(8.dp))

            // Location Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.location_services),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = locationOn,
                    onCheckedChange = onLocationChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.5f)
                    )
                )
            }

            // Network Type Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.network_type, if (is5G) stringResource(R.string.network_5g) else stringResource(R.string.network_4g)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = is5G,
                    onCheckedChange = onNetworkChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.potential_savings, savings),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun InsightCard(insight: InsightEngine.BatteryInsight, accentColor: Color) {
    val borderColor = when (insight.type) {
        InsightEngine.InsightType.CRITICAL -> Color(0xFFFF5252)
        InsightEngine.InsightType.WARNING -> Color(0xFFFFAB40)
        InsightEngine.InsightType.TIP -> Color(0xFF448AFF)
        InsightEngine.InsightType.ANOMALY -> Color(0xFFE040FB)
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(insight.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = stringResource(insight.descriptionRes, *insight.descriptionArgs.toTypedArray()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            insight.causeRes?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.cause_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(it, *insight.causeArgs.toTypedArray()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            insight.fixRes?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.fix_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun WeeklyReportCard(reports: List<DailyReportEntity>, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Trend Visualization
            if (reports.size >= 2) {
                BatteryHealthTrend(reports, accentColor)
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(16.dp))
            }

            reports.take(7).forEach { report ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = report.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Text(
                            text = stringResource(R.string.sot) + ": ${report.screenOnTimeMinutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${report.totalDrain.toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                            Text(
                                text = stringResource(R.string.avg_drain),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${report.avgTemp.toInt()}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (report.avgTemp > 38) Color(0xFFFFAB40) else accentColor
                            )
                            Text(
                                text = stringResource(R.string.avg_temp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
                if (report != reports.last()) {
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryHealthTrend(reports: List<DailyReportEntity>, accentColor: Color) {
    val history = reports.take(7).reversed()
    val maxDrain = history.maxOfOrNull { it.totalDrain }?.coerceAtLeast(1f) ?: 1f
    
    Column {
        Text(
            text = "DRAIN TREND (LAST 7 DAYS)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            history.forEach { report ->
                val barHeight = (report.totalDrain / maxDrain).coerceIn(0.1f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(barHeight)
                            .width(12.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                                ),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = report.date.takeLast(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyAggregatedReportCard(reports: List<DailyReportEntity>, accentColor: Color) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val grouped = reports.groupBy { 
        val date = sdf.parse(it.date) ?: return@groupBy "Unknown"
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        "Week of " + sdf.format(cal.time)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            grouped.forEach { (week, weekReports) ->
                val avgDrain = weekReports.map { it.totalDrain }.average().toFloat()
                val avgTemp = weekReports.map { it.avgTemp }.average().toFloat()
                val totalSot = weekReports.sumOf { it.screenOnTimeMinutes }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = week,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Text(
                            text = "Total SOT: ${totalSot}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${String.format(Locale.US, "%.1f", avgDrain)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                            Text(
                                text = "Avg Drain",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${avgTemp.toInt()}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (avgTemp > 38) Color(0xFFFFAB40) else accentColor
                            )
                            Text(
                                text = "Avg Temp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
                if (week != grouped.keys.last()) {
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthPredictionCard(prediction: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = stringResource(R.string.battery_life_predictor).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                Text(
                    text = prediction,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
