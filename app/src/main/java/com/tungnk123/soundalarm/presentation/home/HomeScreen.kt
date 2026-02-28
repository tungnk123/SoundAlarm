package com.tungnk123.soundalarm.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.presentation.home.components.AlarmItem
import com.tungnk123.soundalarm.presentation.home.components.EmptyStateTutorial
import com.tungnk123.soundalarm.presentation.home.components.SnoozeBannerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAlarmDetail: (Long) -> Unit,
    onNavigateToNewAlarm: () -> Unit,
    onNavigateToSounds: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_app)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewAlarm) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_alarm))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.alarms.isEmpty() && uiState.snoozeState == null -> {
                    EmptyStateTutorial(
                        onNavigateToMusicSelection = onNavigateToSounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.snoozeState?.let { snooze ->
                                SnoozeBannerCard(
                                    alarmLabel = snooze.alarmLabel,
                                    snoozeUntilMs = snooze.snoozeUntilMs,
                                    onCancelSnooze = { viewModel.cancelSnooze() },
                                )
                            }
                            val nextAlarmText = when (val info = uiState.nextAlarmInfo) {
                                is NextAlarmInfo.DaysAndHours -> {
                                    val daysPart = pluralStringResource(R.plurals.next_alarm_days_part, info.days, info.days)
                                    val hoursPart = pluralStringResource(R.plurals.next_alarm_hours_part, info.hours, info.hours)
                                    stringResource(R.string.message_next_alarm_days_hours, daysPart, hoursPart)
                                }
                                is NextAlarmInfo.HoursAndMinutes -> {
                                    val hoursPart = pluralStringResource(R.plurals.next_alarm_hours_part, info.hours, info.hours)
                                    val minutesPart = pluralStringResource(R.plurals.next_alarm_minutes_part, info.minutes, info.minutes)
                                    stringResource(R.string.message_next_alarm_hours_minutes, hoursPart, minutesPart)
                                }
                                is NextAlarmInfo.MinutesOnly -> pluralStringResource(R.plurals.next_alarm_minutes_only, info.minutes, info.minutes)
                                is NextAlarmInfo.Soon -> stringResource(R.string.message_next_alarm_soon)
                                null -> null
                            }
                            nextAlarmText?.let { text ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessAlarm,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(uiState.alarms, key = { it.id }) { alarm ->
                                AlarmItem(
                                    alarm = alarm,
                                    onToggle = { viewModel.toggleAlarm(alarm.id, it) },
                                    onClick = { onNavigateToAlarmDetail(alarm.id) },
                                    onDelete = { viewModel.deleteAlarm(alarm) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}