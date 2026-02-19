package com.tungnk123.soundalarm.presentation.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.presentation.alarm.components.MusicSelectionCard
import com.tungnk123.soundalarm.presentation.alarm.components.ScheduleCard
import com.tungnk123.soundalarm.presentation.alarm.components.TimePickerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlaylist: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    var dayKeyToRemove by remember { mutableStateOf<String?>(null) }

    dayKeyToRemove?.let { dayKey ->
        AlertDialog(
            onDismissRequest = { dayKeyToRemove = null },
            title = { Text(stringResource(R.string.dialog_remove_day_track_title)) },
            text = { Text(stringResource(R.string.dialog_remove_day_track_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeTrackForDay(dayKey)
                    dayKeyToRemove = null
                }) {
                    Text(
                        text = stringResource(R.string.button_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dayKeyToRemove = null }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
        )
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.hour,
            initialMinute = uiState.minute,
            is24Hour = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.label_select_time),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.button_cancel))
                        }
                        TextButton(onClick = {
                            viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) {
                            Text(stringResource(R.string.button_ok))
                        }
                    }
                }
            }
        }
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
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider()
            if (uiState.playlist.isEmpty()) {
                Card(
                    onClick = {
                        viewModel.dismissTrackPicker()
                        onNavigateToPlaylist()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp),
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.label_go_to_playlist),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = stringResource(R.string.message_no_tracks_go_to_playlist),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(uiState.playlist, key = { it.id }) { track ->
                        val isCurrentlySelected =
                            uiState.dayTracks[dayKey]?.trackUri == track.contentUri.toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectTrackForDay(dayKey, track) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isCurrentlySelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isCurrentlySelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrentlySelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (track.artist.isNotBlank() && track.artist != "<unknown>") {
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                            if (isCurrentlySelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
                        else stringResource(R.string.title_alarm_detail_new),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveAlarm() },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = {
                    Text(
                        text = stringResource(R.string.cd_save),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TimePickerCard(
                hour = uiState.hour,
                minute = uiState.minute,
                onClick = { showTimePicker = true },
            )

            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::updateLabel,
                label = { Text(stringResource(R.string.label_alarm_label)) },
                leadingIcon = {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            ScheduleCard(
                repeatDays = uiState.repeatDays,
                isVibrate = uiState.isVibrate,
                deleteAfterFired = uiState.deleteAfterFired,
                onToggleDay = { viewModel.toggleDay(it) },
                onSetOneTime = viewModel::setOneTime,
                onToggleVibrate = { viewModel.toggleVibrate() },
                onToggleDeleteAfterFired = { viewModel.toggleDeleteAfterFired() },
            )

            MusicSelectionCard(
                isRandomMusic = uiState.isRandomMusic,
                dayTracks = uiState.dayTracks,
                repeatDays = uiState.repeatDays,
                onToggleRandomMusic = { viewModel.toggleRandomMusic() },
                onShowTrackPickerForDay = { viewModel.showTrackPickerForDay(it) },
                onRemoveTrackForDay = { dayKeyToRemove = it },
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
