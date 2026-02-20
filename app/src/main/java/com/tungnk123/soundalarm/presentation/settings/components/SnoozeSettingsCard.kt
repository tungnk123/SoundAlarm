package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings

@Composable
fun SnoozeSettingsCard(
    settings: AppSettings,
    onSnoozeCountChange: (Int) -> Unit,
    onAlarmDurationChange: (Int) -> Unit,
) {
    SettingsCard(title = stringResource(R.string.settings_section_snooze), icon = Icons.Default.Snooze) {
        SettingsRow(
            icon = Icons.Default.Snooze,
            title = stringResource(R.string.settings_snooze_count),
            description = stringResource(R.string.settings_snooze_count_desc),
        ) {
            ChipRow(
                options = listOf(1, 2, 3, 5, 0),
                selected = settings.snoozeCount,
                label = { if (it == 0) stringResource(R.string.settings_unlimited) else it.toString() },
                onSelect = onSnoozeCountChange,
            )
        }
        SettingsDivider()
        SettingsRow(
            icon = Icons.Default.Timer,
            title = stringResource(R.string.settings_alarm_duration),
            description = stringResource(R.string.settings_alarm_duration_desc),
        ) {
            ChipRow(
                options = listOf(1, 5, 10, 15, 30),
                selected = settings.alarmDuration,
                label = { stringResource(R.string.settings_minutes, it) },
                onSelect = onAlarmDurationChange,
            )
        }
    }
}
