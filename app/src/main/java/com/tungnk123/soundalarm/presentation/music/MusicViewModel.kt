package com.tungnk123.soundalarm.presentation.music

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.repository.MusicRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MusicUiState {
    data object Loading : MusicUiState
    data class Success(val tracks: List<MusicTrack>, val currentPlayingUri: Uri? = null) : MusicUiState
    data class Error(val message: String) : MusicUiState
    data object PermissionRequired : MusicUiState
}

sealed interface MusicEvent {
    data class AddedToPlaylist(val message: String) : MusicEvent
    data class AddError(val message: String) : MusicEvent
}

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val audioPlayer: MusicAudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Loading)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MusicEvent>()
    val events: SharedFlow<MusicEvent> = _events.asSharedFlow()

    fun addToPlaylist(track: MusicTrack) {
        viewModelScope.launch {
            try {
                playlistRepository.addTrack(track)
                _events.emit(MusicEvent.AddedToPlaylist(context.getString(R.string.message_track_added, track.title)))
            } catch (e: Exception) {
                _events.emit(MusicEvent.AddError(context.getString(R.string.message_add_error)))
            }
        }
    }

    fun onPermissionGranted() {
        loadMusic()
    }

    fun onPermissionDenied() {
        _uiState.value = MusicUiState.PermissionRequired
    }

    private fun loadMusic() {
        viewModelScope.launch {
            _uiState.value = MusicUiState.Loading
            try {
                val tracks = repository.getLocalMusicTracks()
                _uiState.value = MusicUiState.Success(tracks)
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun playTrack(track: MusicTrack) {
        audioPlayer.play(track.contentUri)
        val currentState = _uiState.value
        if (currentState is MusicUiState.Success) {
            _uiState.value = currentState.copy(currentPlayingUri = track.contentUri)
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        val currentState = _uiState.value
        if (currentState is MusicUiState.Success) {
            _uiState.value = currentState.copy(currentPlayingUri = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
