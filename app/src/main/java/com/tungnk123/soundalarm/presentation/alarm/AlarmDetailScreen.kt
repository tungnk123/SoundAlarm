package com.tungnk123.soundalarm.presentation.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.model.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timePickerState = key(uiState.isEditing) {
        rememberTimePickerState(
            initialHour = uiState.hour,
            initialMinute = uiState.minute,
            is24Hour = true,
        )
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    val trackPickerSheetState = rememberModalBottomSheetState()
    val defaultLabel = stringResource(R.string.label_default)

    if (uiState.showTrackPickerForDay != null) {
        val dayKey = uiState.showTrackPickerForDay!!
        val dayLabel = if (dayKey == AlarmDayTrack.DEFAULT_DAY) defaultLabel
        else DayOfWeek.valueOf(dayKey).shortName

        ModalBottomSheet(
            onDismissRequest = viewModel::dismissTrackPicker,
            sheetState = trackPickerSheetState,
        ) {
            Text(
                text = stringResource(R.string.message_select_track_for, dayLabel),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()
            if (uiState.playlist.isEmpty()) {
                Text(
                    text = stringResource(R.string.message_no_tracks_in_playlist),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(uiState.playlist, key = { it.id }) { track ->
                        ListItem(
                            headlineContent = { Text(track.title) },
                            supportingContent = if (track.artist.isNotBlank() && track.artist != "<unknown>") {
                                { Text(track.artist) }
                            } else null,
                            leadingContent = {
                                Icon(Icons.Default.MusicNote, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                viewModel.selectTrackForDay(dayKey, track)
                            },
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) stringResource(R.string.title_alarm_detail_edit)
                        else stringResource(R.string.title_alarm_detail_new)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                        viewModel.saveAlarm()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.cd_save))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimePicker(state = timePickerState)

            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::updateLabel,
                label = { Text(stringResource(R.string.label_alarm_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text(
                text = stringResource(R.string.label_repeat),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in uiState.repeatDays,
                        onClick = { viewModel.toggleDay(day) },
                        label = { Text(day.shortName) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.label_vibrate), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.isVibrate,
                    onCheckedChange = { viewModel.toggleVibrate() },
                )
            }

            HorizontalDivider()
            Text(
                text = stringResource(R.string.label_music),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.message_music_selection_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )

            DayTrackRow(
                dayKey = AlarmDayTrack.DEFAULT_DAY,
                label = stringResource(R.string.label_default_all_days),
                selection = uiState.dayTracks[AlarmDayTrack.DEFAULT_DAY],
                onSelectTrack = { viewModel.showTrackPickerForDay(AlarmDayTrack.DEFAULT_DAY) },
                onRemoveTrack = { viewModel.removeTrackForDay(AlarmDayTrack.DEFAULT_DAY) },
            )

            if (uiState.repeatDays.isNotEmpty()) {
                DayOfWeek.entries
                    .filter { it in uiState.repeatDays }
                    .forEach { day ->
                        DayTrackRow(
                            dayKey = day.name,
                            label = day.shortName,
                            selection = uiState.dayTracks[day.name],
                            onSelectTrack = { viewModel.showTrackPickerForDay(day.name) },
                            onRemoveTrack = { viewModel.removeTrackForDay(day.name) },
                        )
                    }
            }
        }
    }
}

@Composable
private fun DayTrackRow(
    dayKey: String,
    label: String,
    selection: TrackSelection?,
    onSelectTrack: () -> Unit,
    onRemoveTrack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectTrack)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = if (selection != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (selection != null) {
                Text(
                    text = "${selection.trackTitle} — ${selection.trackArtist}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            } else {
                Text(
                    text = stringResource(R.string.message_tap_select_track),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (selection != null) {
            IconButton(onClick = onRemoveTrack) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_remove_track))
            }
        } else {
            TextButton(onClick = onSelectTrack) {
                Text(stringResource(R.string.button_select))
            }
        }
    }
}
