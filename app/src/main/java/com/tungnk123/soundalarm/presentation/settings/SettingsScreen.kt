package com.tungnk123.soundalarm.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.presentation.settings.components.AccessibilitySettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.CustomVoiceSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.LanguageSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.NotificationSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.PowerSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.SnoozeSettingsCard
import com.tungnk123.soundalarm.presentation.settings.components.SoundSettingsCard
import com.tungnk123.soundalarm.util.LocaleManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    val currentLanguage = remember(context) { LocaleManager.getLanguage(context) }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importCustomVoice(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
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
            AccessibilitySettingsCard(
                settings = settings,
                onReadTimeAloudChange = viewModel::updateReadTimeAloud,
                onTimeAnnouncementTemplateChange = viewModel::updateTimeAnnouncementTemplate,
            )
            CustomVoiceSettingsCard(
                settings = settings,
                onPickVoice = { voiceLauncher.launch("audio/*") },
                onRemoveVoice = viewModel::removeCustomVoice,
                onVoiceBeforeMusicChange = viewModel::updateVoiceBeforeMusic,
            )
            LanguageSettingsCard(
                currentLanguage = currentLanguage,
                onLanguageChange = { lang ->
                    LocaleManager.setLanguage(context, lang)
                    activity?.recreate()
                },
            )
        }
    }
}
