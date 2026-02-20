package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings

@Composable
fun NotificationSettingsCard(
    settings: AppSettings,
    onPreAlarmNotificationChange: (Boolean) -> Unit,
    onPreAlarmNotificationTimeChange: (Int) -> Unit,
) {
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
                    ChipRow(
                        options = listOf(5, 10, 15, 30),
                        selected = settings.preAlarmNotificationTime,
                        label = { stringResource(R.string.settings_minutes, it) },
                        onSelect = onPreAlarmNotificationTimeChange,
                    )
                }
            }
        }
    }
}
