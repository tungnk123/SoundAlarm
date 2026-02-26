package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings

private val NOTIFY_TIME_PRESETS = listOf(5, 10, 15, 30, 60)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsCard(
    settings: AppSettings,
    onPreAlarmNotificationChange: (Boolean) -> Unit,
    onPreAlarmNotificationTimeChange: (Int) -> Unit,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        NotifyTimeCustomDialog(
            initialMinutes = if (settings.preAlarmNotificationTime !in NOTIFY_TIME_PRESETS)
                settings.preAlarmNotificationTime else 0,
            onConfirm = { minutes ->
                onPreAlarmNotificationTimeChange(minutes)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }

    SettingsCard(title = stringResource(R.string.settings_section_notification), icon = Icons.Default.Alarm) {
        SettingsSwitchRow(
            icon = Icons.Default.Alarm,
            title = stringResource(R.string.settings_pre_alarm_notification),
            description = stringResource(R.string.settings_pre_alarm_notification_desc),
            checked = settings.preAlarmNotification,
            onCheckedChange = onPreAlarmNotificationChange,
        )
        AnimatedVisibility(visible = settings.preAlarmNotification) {
            Column {
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Timer,
                    title = stringResource(R.string.settings_pre_alarm_notification_time),
                    description = stringResource(R.string.settings_pre_alarm_notification_time_desc),
                ) {
                    val isCustomSelected = settings.preAlarmNotificationTime !in NOTIFY_TIME_PRESETS
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        NOTIFY_TIME_PRESETS.forEach { minutes ->
                            FilterChip(
                                selected = settings.preAlarmNotificationTime == minutes,
                                onClick = { onPreAlarmNotificationTimeChange(minutes) },
                                label = { Text(stringResource(R.string.settings_minutes, minutes)) },
                            )
                        }
                        FilterChip(
                            selected = isCustomSelected,
                            onClick = { showCustomDialog = true },
                            label = {
                                Text(
                                    if (isCustomSelected)
                                        stringResource(R.string.settings_minutes, settings.preAlarmNotificationTime)
                                    else
                                        stringResource(R.string.label_notify_before_custom),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifyTimeCustomDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(if (initialMinutes > 0) initialMinutes.toString() else "") }
    val parsed = text.toIntOrNull()
    val isValid = parsed != null && parsed > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_notify_before_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.dialog_notify_before_label)) },
                suffix = { Text(stringResource(R.string.dialog_notify_before_suffix)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = text.isNotEmpty() && !isValid,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onConfirm(parsed!!) },
                enabled = isValid,
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
    )
}
