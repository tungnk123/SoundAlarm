package com.tungnk123.soundalarm.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmEvent
import com.tungnk123.soundalarm.domain.model.AlarmEventType
import com.tungnk123.soundalarm.domain.model.AlarmStatistics
import com.tungnk123.soundalarm.domain.model.DismissMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_statistics)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.cd_clear_statistics),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        StatisticsContent(
            stats = uiState,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.statistics_clear_title)) },
            text = { Text(stringResource(R.string.statistics_clear_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearStatistics()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.button_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
        )
    }
}

@Composable
private fun StatisticsContent(
    stats: AlarmStatistics,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        item {
            SummarySection(
                totalAlarms = stats.totalAlarms,
                activeAlarms = stats.activeAlarms,
                totalDismissed = stats.totalDismissed,
                totalSnoozed = stats.totalSnoozed,
            )
        }

        item {
            ChallengeBreakdownSection(
                stats = stats,
            )
        }

        if (stats.recentEvents.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.statistics_recent_activity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(stats.recentEvents, key = { it.id }) { event ->
                RecentEventItem(event = event)
            }
        } else {
            item {
                EmptyEventsCard()
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SummarySection(
    totalAlarms: Int,
    activeAlarms: Int,
    totalDismissed: Int,
    totalSnoozed: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.statistics_summary),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard(
                label = stringResource(R.string.statistics_total_alarms),
                value = totalAlarms.toString(),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.statistics_active_alarms),
                value = activeAlarms.toString(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard(
                label = stringResource(R.string.statistics_dismissed),
                value = totalDismissed.toString(),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.statistics_snoozed),
                value = totalSnoozed.toString(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ChallengeBreakdownSection(stats: AlarmStatistics) {
    val methods = DismissMethod.entries
    val maxDismissed = methods.maxOfOrNull { stats.dismissByMethod[it] ?: 0 }.takeIf { it != 0 } ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.statistics_challenge_breakdown),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                methods.forEach { method ->
                    ChallengeMethodRow(
                        method = method,
                        dismissedCount = stats.dismissByMethod[method] ?: 0,
                        alarmCount = stats.alarmsByMethod[method] ?: 0,
                        maxDismissed = maxDismissed,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeMethodRow(
    method: DismissMethod,
    dismissedCount: Int,
    alarmCount: Int,
    maxDismissed: Int,
) {
    val methodColor = methodColor(method)
    val methodLabel = methodLabel(method)
    val progress = (dismissedCount.toFloat() / maxDismissed).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(methodColor),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = methodLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.statistics_alarms_count, alarmCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Text(
                    text = stringResource(R.string.statistics_dismissed_count, dismissedCount),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(methodColor),
                )
            }
        }
    }
}

@Composable
private fun RecentEventItem(event: AlarmEvent) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(eventColor(event.eventType).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (event.eventType) {
                        AlarmEventType.FIRED -> Icons.Filled.NotificationsActive
                        AlarmEventType.DISMISSED -> Icons.Filled.Check
                        AlarmEventType.SNOOZED -> Icons.Filled.Snooze
                    },
                    contentDescription = null,
                    tint = eventColor(event.eventType),
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.alarmLabel.ifEmpty { stringResource(R.string.label_alarm_default) },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = eventLabel(event),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = dateFormat.format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun EmptyEventsCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Text(
                text = stringResource(R.string.statistics_no_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun methodColor(method: DismissMethod): Color = when (method) {
    DismissMethod.NONE -> MaterialTheme.colorScheme.outline
    DismissMethod.MATH -> MaterialTheme.colorScheme.primary
    DismissMethod.SHAKE -> Color(0xFFE65100)
    DismissMethod.WALK -> Color(0xFF2E7D32)
    DismissMethod.MEMORY -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun methodLabel(method: DismissMethod): String = when (method) {
    DismissMethod.NONE -> stringResource(R.string.dismiss_method_none)
    DismissMethod.MATH -> stringResource(R.string.dismiss_method_math)
    DismissMethod.SHAKE -> stringResource(R.string.dismiss_method_shake)
    DismissMethod.WALK -> stringResource(R.string.dismiss_method_walk)
    DismissMethod.MEMORY -> stringResource(R.string.dismiss_method_memory)
}

@Composable
private fun eventColor(eventType: AlarmEventType): Color = when (eventType) {
    AlarmEventType.FIRED -> MaterialTheme.colorScheme.primary
    AlarmEventType.DISMISSED -> Color(0xFF2E7D32)
    AlarmEventType.SNOOZED -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun eventLabel(event: AlarmEvent): String {
    val method = methodLabel(event.dismissMethod)
    return when (event.eventType) {
        AlarmEventType.FIRED -> stringResource(R.string.statistics_event_fired)
        AlarmEventType.SNOOZED -> stringResource(R.string.statistics_event_snoozed)
        AlarmEventType.DISMISSED -> if (event.dismissMethod == DismissMethod.NONE) {
            stringResource(R.string.statistics_event_dismissed)
        } else {
            stringResource(R.string.statistics_event_dismissed_with, method)
        }
    }
}
