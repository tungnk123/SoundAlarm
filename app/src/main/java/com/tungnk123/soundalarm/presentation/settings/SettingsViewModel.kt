package com.tungnk123.soundalarm.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import com.tungnk123.soundalarm.domain.repository.PlaylistGroupRepository
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    playlistGroupRepository: PlaylistGroupRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, settingsRepository.getSettings())

    val playlists: StateFlow<List<PlaylistGroup>> = playlistGroupRepository.getAllPlaylistGroups()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSnoozeCount(count: Int) {
        viewModelScope.launch { settingsRepository.updateSnoozeCount(count) }
    }

    fun updateAlarmDuration(minutes: Int) {
        viewModelScope.launch { settingsRepository.updateAlarmDuration(minutes) }
    }

    fun updatePreAlarmNotification(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updatePreAlarmNotification(enabled) }
    }

    fun updatePreAlarmNotificationTime(minutes: Int) {
        viewModelScope.launch { settingsRepository.updatePreAlarmNotificationTime(minutes) }
    }

    fun updateAlarmWhenPowerOff(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateAlarmWhenPowerOff(enabled) }
    }

    fun updateDefaultVolume(volume: Float) {
        viewModelScope.launch { settingsRepository.updateDefaultVolume(volume) }
    }

    fun updateDefaultFadeInDuration(seconds: Int) {
        viewModelScope.launch { settingsRepository.updateDefaultFadeInDuration(seconds) }
    }

    fun updateDefaultFadeOutDuration(seconds: Int) {
        viewModelScope.launch { settingsRepository.updateDefaultFadeOutDuration(seconds) }
    }

    fun updateDefaultPlaylistId(id: Long) {
        viewModelScope.launch { settingsRepository.updateDefaultPlaylistId(id) }
    }
}
