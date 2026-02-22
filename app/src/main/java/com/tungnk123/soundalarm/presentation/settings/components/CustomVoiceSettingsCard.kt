package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings
import java.io.File

@Composable
fun CustomVoiceSettingsCard(
    settings: AppSettings,
    onPickVoice: () -> Unit,
    onRemoveVoice: () -> Unit,
    onVoiceBeforeMusicChange: (Boolean) -> Unit,
) {
    val hasVoice = settings.customVoiceAudioPath.isNotEmpty()
    val fileName = if (hasVoice) File(settings.customVoiceAudioPath).name else null

    SettingsCard(
        title = stringResource(R.string.settings_section_custom_voice),
        icon = Icons.Default.MicNone,
    ) {
        Text(
            text = stringResource(R.string.settings_custom_voice_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasVoice && fileName != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onRemoveVoice) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.settings_custom_voice_remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            SettingsSwitchRow(
                icon = Icons.Default.QueueMusic,
                title = stringResource(R.string.settings_custom_voice_before_music),
                description = stringResource(R.string.settings_custom_voice_before_music_desc),
                checked = settings.voiceBeforeMusic,
                onCheckedChange = onVoiceBeforeMusicChange,
            )
            OutlinedButton(
                onClick = onPickVoice,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_custom_voice_change))
            }
        } else {
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onPickVoice,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_custom_voice_pick))
            }
        }
    }
}
