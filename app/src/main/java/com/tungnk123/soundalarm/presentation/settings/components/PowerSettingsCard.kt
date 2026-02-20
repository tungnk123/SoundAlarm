package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings

@Composable
fun PowerSettingsCard(
    settings: AppSettings,
    onAlarmWhenPowerOffChange: (Boolean) -> Unit,
) {
    SettingsCard(title = stringResource(R.string.settings_section_power), icon = Icons.Default.PowerSettingsNew) {
        SettingsSwitchRow(
            icon = Icons.Default.PowerSettingsNew,
            title = stringResource(R.string.settings_alarm_when_power_off),
            description = stringResource(R.string.settings_alarm_when_power_off_desc),
            checked = settings.alarmWhenPowerOff,
            onCheckedChange = onAlarmWhenPowerOffChange,
        )
        AnimatedVisibility(visible = settings.alarmWhenPowerOff) {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(start = 40.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.settings_alarm_when_power_off_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
