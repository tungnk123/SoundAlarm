package com.tungnk123.soundalarm.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.presentation.settings.components.NotificationSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.PowerSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.SnoozeSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.SoundSettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SnoozeSettingsCard(
                settings = settings,
                onSnoozeCountChange = viewModel::updateSnoozeCount,
                onAlarmDurationChange = viewModel::updateAlarmDuration,
            )
            SoundSettingsCard(
                settings = settings,
                playlists = playlists,
                onVolumeChange = viewModel::updateDefaultVolume,
                onFadeInChange = viewModel::updateDefaultFadeInDuration,
                onFadeOutChange = viewModel::updateDefaultFadeOutDuration,
                onDefaultPlaylistChange = viewModel::updateDefaultPlaylistId,
            )
            NotificationSettingsCard(
                settings = settings,
                onPreAlarmNotificationChange = viewModel::updatePreAlarmNotification,
                onPreAlarmNotificationTimeChange = viewModel::updatePreAlarmNotificationTime,
            )
            PowerSettingsCard(
                settings = settings,
                onAlarmWhenPowerOffChange = viewModel::updateAlarmWhenPowerOff,
            )
        }
    }
}
