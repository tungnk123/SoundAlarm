package com.tungnk123.soundalarm.presentation.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.model.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                icon = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                    )
                },
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
            Card(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "%02d:%02d".format(uiState.hour, uiState.minute),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Light,
                        )
                        Text(
                            text = stringResource(R.string.label_tap_to_change_time),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

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

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column {
                    // Repeat
                    SectionHeader(
                        icon = {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        title = stringResource(R.string.label_repeat),
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // "One time" chip
                        FilterChip(
                            selected = uiState.repeatDays.isEmpty(),
                            onClick = viewModel::setOneTime,
                            label = { Text(stringResource(R.string.label_one_time)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        )
                        DayOfWeek.entries.forEach { day ->
                            FilterChip(
                                selected = day in uiState.repeatDays,
                                onClick = { viewModel.toggleDay(day) },
                                label = { Text(day.shortName) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.label_vibrate)) },
                        leadingContent = {
                            Icon(Icons.Default.VolumeUp, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = uiState.isVibrate,
                                onCheckedChange = { viewModel.toggleVibrate() },
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    )

                    // Delete after alarm — only for one-time alarms
                    AnimatedVisibility(
                        visible = uiState.repeatDays.isEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.label_delete_after_alarm)) },
                                supportingContent = { Text(stringResource(R.string.label_delete_after_alarm_desc)) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = if (uiState.deleteAfterFired)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.deleteAfterFired,
                                        onCheckedChange = { viewModel.toggleDeleteAfterFired() },
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                ),
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column {
                    SectionHeader(
                        icon = {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        title = stringResource(R.string.label_music),
                        subtitle = stringResource(R.string.message_music_selection_help),
                    )

                    // Random music toggle
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.label_random_music)) },
                        supportingContent = { Text(stringResource(R.string.label_random_music_desc)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = if (uiState.isRandomMusic)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = uiState.isRandomMusic,
                                onCheckedChange = { viewModel.toggleRandomMusic() },
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    )

                    AnimatedVisibility(
                        visible = !uiState.isRandomMusic,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            DayTrackRow(
                                label = stringResource(R.string.label_default_all_days),
                                selection = uiState.dayTracks[AlarmDayTrack.DEFAULT_DAY],
                                onSelectTrack = { viewModel.showTrackPickerForDay(AlarmDayTrack.DEFAULT_DAY) },
                                onRemoveTrack = { dayKeyToRemove = AlarmDayTrack.DEFAULT_DAY },
                                showDivider = false,
                            )

                            if (uiState.repeatDays.isNotEmpty()) {
                                DayOfWeek.entries
                                    .filter { it in uiState.repeatDays }
                                    .forEach { day ->
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        DayTrackRow(
                                            label = day.shortName,
                                            selection = uiState.dayTracks[day.name],
                                            onSelectTrack = { viewModel.showTrackPickerForDay(day.name) },
                                            onRemoveTrack = { dayKeyToRemove = day.name },
                                            showDivider = false,
                                        )
                                    }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Extra bottom padding so content isn't hidden behind FAB
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (subtitle != null) 2.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(18.dp)) { icon() }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayTrackRow(
    label: String,
    selection: TrackSelection?,
    onSelectTrack: () -> Unit,
    onRemoveTrack: () -> Unit,
    showDivider: Boolean = true,
) {
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = {
            if (selection != null) {
                Text(
                    text = "${selection.trackTitle} — ${selection.trackArtist}",
                    maxLines = 1,
                )
            } else {
                Text(text = stringResource(R.string.message_tap_select_track))
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (selection != null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            if (selection != null) {
                IconButton(onClick = onRemoveTrack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_remove_track)
                    )
                }
            } else {
                TextButton(onClick = onSelectTrack) {
                    Text(stringResource(R.string.button_select))
                }
            }
        },
        modifier = Modifier.clickable(onClick = onSelectTrack),
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}
