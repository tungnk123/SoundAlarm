package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings

@Composable
fun AccessibilitySettingsCard(
    settings: AppSettings,
    onReadTimeAloudChange: (Boolean) -> Unit,
    onTimeAnnouncementTemplateChange: (String) -> Unit,
) {
    SettingsCard(
        title = stringResource(R.string.settings_section_accessibility),
        icon = Icons.Default.RecordVoiceOver,
    ) {
        SettingsSwitchRow(
            icon = Icons.Default.RecordVoiceOver,
            title = stringResource(R.string.settings_read_time_aloud),
            description = stringResource(R.string.settings_read_time_aloud_desc),
            checked = settings.readTimeAloud,
            onCheckedChange = onReadTimeAloudChange,
        )
        AnimatedVisibility(visible = settings.readTimeAloud) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = settings.timeAnnouncementTemplate,
                    onValueChange = onTimeAnnouncementTemplateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_announcement_template)) },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.settings_announcement_template_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                )
            }
        }
    }
}
