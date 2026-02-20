package com.tungnk123.soundalarm.presentation.alarm

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import com.tungnk123.soundalarm.domain.model.TrackSelection
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistGroupRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import com.tungnk123.soundalarm.domain.usecase.SaveAlarmUseCase
import com.tungnk123.soundalarm.presentation.music.MusicAudioPlayer
import com.tungnk123.soundalarm.util.AppConstants.DELAY_PREVIEW_MUSIC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AlarmDetailUiState(
    val hour: Int = LocalTime.now().hour,
    val minute: Int = LocalTime.now().minute,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val isVibrate: Boolean = true,
    val deleteAfterFired: Boolean = false,
    val soundUri: String? = null,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val dayTracks: Map<String, TrackSelection> = emptyMap(),
    val playlist: List<MusicTrack> = emptyList(),
    val showTrackPickerForDay: String? = null,
    val isRandomMusic: Boolean = false,
    val volume: Float = 1.0f,
    val fadeInDuration: Int = 0,
    val playlistGroupId: Long = 0,
    val availablePlaylistGroups: List<PlaylistGroup> = emptyList(),
)

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playlistGroupRepository: PlaylistGroupRepository,
    private val alarmDayTrackRepository: AlarmDayTrackRepository,
    private val musicAudioPlayer: MusicAudioPlayer,
) : ViewModel() {

    private val alarmId: Long = savedStateHandle["alarmId"] ?: 0L
    private var previewJob: Job? = null

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlaylist()
        loadAvailablePlaylistGroups()
        if (alarmId != 0L) {
            loadAlarm()
            loadDayTracks()
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            playlistRepository.getPlaylist().collect { tracks ->
                _uiState.update { it.copy(playlist = tracks) }
            }
        }
    }

    private fun loadAvailablePlaylistGroups() {
        viewModelScope.launch {
            playlistGroupRepository.getAllPlaylistGroups().collect { groups ->
                _uiState.update { it.copy(availablePlaylistGroups = groups) }
            }
        }
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            alarmRepository.getAlarmById(alarmId)?.let { alarm ->
                _uiState.update {
                    it.copy(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        label = alarm.label,
                        repeatDays = alarm.repeatDays,
                        isVibrate = alarm.isVibrate,
                        deleteAfterFired = alarm.deleteAfterFired,
                        soundUri = alarm.soundUri,
                        isEditing = true,
                        isRandomMusic = alarm.isRandomMusic,
                        volume = alarm.volume,
                        fadeInDuration = alarm.fadeInDuration,
                        playlistGroupId = alarm.playlistGroupId,
                    )
                }
            }
        }
    }

    private fun loadDayTracks() {
        viewModelScope.launch {
            alarmDayTrackRepository.getTracksForAlarm(alarmId).collect { tracks ->
                val map = tracks.associate { track ->
                    track.dayOfWeek to TrackSelection(
                        trackUri = track.trackUri,
                        trackTitle = track.trackTitle,
                        trackArtist = track.trackArtist,
                    )
                }
                _uiState.update { it.copy(dayTracks = map) }
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update { state ->
            val newDays = state.repeatDays.toMutableSet()
            if (day in newDays) newDays.remove(day) else newDays.add(day)
            state.copy(
                repeatDays = newDays,
                deleteAfterFired = if (newDays.isNotEmpty()) false else state.deleteAfterFired,
            )
        }
    }

    fun setOneTime() {
        _uiState.update { it.copy(repeatDays = emptySet()) }
    }

    fun toggleVibrate() {
        _uiState.update { it.copy(isVibrate = !it.isVibrate) }
    }

    fun toggleDeleteAfterFired() {
        _uiState.update { it.copy(deleteAfterFired = !it.deleteAfterFired) }
    }

    fun toggleRandomMusic() {
        _uiState.update { it.copy(isRandomMusic = !it.isRandomMusic) }
    }

    fun updateVolume(volume: Float) {
        _uiState.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun previewVolume(volume: Float) {
        val state = _uiState.value
        val uri = state.soundUri?.toUri() ?: state.playlist.firstOrNull()?.contentUri ?: return
        previewJob?.cancel()
        musicAudioPlayer.play(uri = uri, volume = volume, fadeInDuration = 0)
        previewJob = viewModelScope.launch {
            delay(DELAY_PREVIEW_MUSIC)
            musicAudioPlayer.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        previewJob?.cancel()
        musicAudioPlayer.stop()
    }

    fun updateFadeInDuration(seconds: Int) {
        _uiState.update { it.copy(fadeInDuration = seconds) }
    }

    fun updatePlaylistGroup(playlistGroupId: Long) {
        _uiState.update { it.copy(playlistGroupId = playlistGroupId) }
    }

    fun showTrackPickerForDay(dayKey: String) {
        _uiState.update { it.copy(showTrackPickerForDay = dayKey) }
    }

    fun dismissTrackPicker() {
        _uiState.update { it.copy(showTrackPickerForDay = null) }
    }

    fun selectTrackForDay(dayKey: String, track: MusicTrack) {
        _uiState.update { state ->
            val newDayTracks = state.dayTracks.toMutableMap()
            newDayTracks[dayKey] = TrackSelection(
                trackUri = track.contentUri.toString(),
                trackTitle = track.title,
                trackArtist = track.artist,
            )
            state.copy(dayTracks = newDayTracks, showTrackPickerForDay = null)
        }
    }

    fun removeTrackForDay(dayKey: String) {
        _uiState.update { state ->
            val newDayTracks = state.dayTracks.toMutableMap()
            newDayTracks.remove(dayKey)
            state.copy(dayTracks = newDayTracks)
        }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val state = _uiState.value
            val alarm = Alarm(
                id = alarmId,
                hour = state.hour,
                minute = state.minute,
                label = state.label,
                repeatDays = state.repeatDays,
                isVibrate = state.isVibrate,
                deleteAfterFired = state.deleteAfterFired,
                soundUri = state.soundUri,
                isRandomMusic = state.isRandomMusic,
                volume = state.volume,
                fadeInDuration = state.fadeInDuration,
                playlistGroupId = state.playlistGroupId,
            )
            val savedId = saveAlarmUseCase(alarm)

            alarmDayTrackRepository.deleteAllTracksForAlarm(savedId)
            state.dayTracks.forEach { (dayKey, selection) ->
                alarmDayTrackRepository.setTrackForDay(
                    alarmId = savedId,
                    dayOfWeek = dayKey,
                    trackUri = selection.trackUri,
                    trackTitle = selection.trackTitle,
                    trackArtist = selection.trackArtist,
                )
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
