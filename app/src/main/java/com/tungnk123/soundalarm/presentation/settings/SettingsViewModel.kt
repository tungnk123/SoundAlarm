package com.tungnk123.soundalarm.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import com.tungnk123.soundalarm.domain.repository.PlaylistGroupRepository
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    fun updateReadTimeAloud(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateReadTimeAloud(enabled) }
    }

    fun updateTimeAnnouncementTemplate(template: String) {
        viewModelScope.launch { settingsRepository.updateTimeAnnouncementTemplate(template) }
    }

    fun importCustomVoice(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val dest = File(context.filesDir, "custom_voice_audio")
                inputStream.use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                settingsRepository.updateCustomVoiceAudioPath(dest.absolutePath)
            } catch (_: Exception) {
            }
        }
    }

    fun updateVoiceBeforeMusic(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateVoiceBeforeMusic(enabled) }
    }

    fun removeCustomVoice() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = settingsRepository.getSettings().customVoiceAudioPath
            if (path.isNotEmpty()) File(path).delete()
            settingsRepository.updateCustomVoiceAudioPath("")
        }
    }
}
