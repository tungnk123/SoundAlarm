package com.tungnk123.soundalarm.presentation.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import kotlin.math.roundToInt

@Composable
fun SoundSettingsCard(
    settings: AppSettings,
    playlists: List<PlaylistGroup>,
    onVolumeChange: (Float) -> Unit,
    onFadeInChange: (Int) -> Unit,
    onFadeOutChange: (Int) -> Unit,
    onDefaultPlaylistChange: (Long) -> Unit,
) {
    SettingsCard(title = stringResource(R.string.settings_section_sound), icon = Icons.AutoMirrored.Filled.VolumeUp) {
        SettingsRow(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            title = stringResource(R.string.settings_default_volume),
            description = stringResource(R.string.settings_default_volume_desc),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Slider(
                    value = settings.defaultVolume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(settings.defaultVolume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SettingsDivider()
        SettingsRow(
            icon = Icons.Default.LibraryMusic,
            title = stringResource(R.string.settings_default_playlist),
            description = stringResource(R.string.settings_default_playlist_desc),
        ) {
            val options = listOf(AppSettings.NO_DEFAULT_PLAYLIST) + playlists.map { it.id }
            ChipRow(
                options = options,
                selected = settings.defaultPlaylistId,
                label = { id ->
                    if (id == AppSettings.NO_DEFAULT_PLAYLIST) {
                        stringResource(R.string.settings_no_playlist)
                    } else {
                        playlists.find { it.id == id }?.name ?: stringResource(R.string.settings_no_playlist)
                    }
                },
                onSelect = onDefaultPlaylistChange,
            )
        }
        SettingsDivider()
        SettingsRow(
            icon = Icons.Default.TrendingUp,
            title = stringResource(R.string.settings_default_fade_in),
            description = stringResource(R.string.settings_default_fade_in_desc),
        ) {
            ChipRow(
                options = listOf(0, 15, 30, 60, 120),
                selected = settings.defaultFadeInDuration,
                label = { fadeDurationLabel(it) },
                onSelect = onFadeInChange,
            )
        }
        SettingsDivider()
        SettingsRow(
            icon = Icons.Default.TrendingDown,
            title = stringResource(R.string.settings_default_fade_out),
            description = stringResource(R.string.settings_default_fade_out_desc),
        ) {
            ChipRow(
                options = listOf(0, 5, 10, 15, 30),
                selected = settings.defaultFadeOutDuration,
                label = { fadeDurationLabel(it) },
                onSelect = onFadeOutChange,
            )
        }
    }
}
