package com.tungnk123.soundalarm.presentation.playlist

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistEvent {
    data class TrackRemoved(val message: String) : PlaylistEvent
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val repository: PlaylistRepository,
) : ViewModel() {

    val playlistGroupId: Long = savedStateHandle["playlistGroupId"] ?: 0L

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val playlist: StateFlow<List<MusicTrack>> =
        repository.getTracksByPlaylistGroup(playlistGroupId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val favoriteTracks: StateFlow<List<MusicTrack>> =
        repository.getTracksByPlaylistGroup(playlistGroupId)
            .map { tracks -> tracks.filter { it.isFavorite } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _events = MutableSharedFlow<PlaylistEvent>()
    val events: SharedFlow<PlaylistEvent> = _events.asSharedFlow()

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.update { !it }
    }

    fun toggleFavorite(track: MusicTrack) {
        viewModelScope.launch {
            repository.setFavorite(track.id, !track.isFavorite)
        }
    }

    fun removeTrack(track: MusicTrack) {
        viewModelScope.launch {
            repository.removeTrack(track)
            _events.emit(PlaylistEvent.TrackRemoved(context.getString(R.string.message_track_removed, track.title)))
        }
    }

    fun saveOrder(tracks: List<MusicTrack>) {
        viewModelScope.launch {
            repository.updateTrackOrder(tracks)
        }
    }
}
