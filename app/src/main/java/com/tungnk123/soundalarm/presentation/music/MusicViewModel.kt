package com.tungnk123.soundalarm.presentation.music

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MusicUiState {
    data object Loading : MusicUiState
    data class Success(val tracks: List<MusicTrack>, val currentPlayingUri: Uri? = null) : MusicUiState
    data class Error(val message: String) : MusicUiState
    data object PermissionRequired : MusicUiState
}

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playlistRepository: com.tungnk123.soundalarm.domain.repository.PlaylistRepository, // Add explicit type or import
    private val audioPlayer: MusicAudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Loading)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun addToPlaylist(track: MusicTrack) {
        viewModelScope.launch {
            playlistRepository.addTrack(track)
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
