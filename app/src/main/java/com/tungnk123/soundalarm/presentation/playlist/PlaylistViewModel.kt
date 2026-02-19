package com.tungnk123.soundalarm.presentation.playlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistEvent {
    data class TrackRemoved(val message: String) : PlaylistEvent
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PlaylistRepository
) : ViewModel() {

    val playlist: StateFlow<List<MusicTrack>> = repository.getPlaylist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _events = MutableSharedFlow<PlaylistEvent>()
    val events: SharedFlow<PlaylistEvent> = _events.asSharedFlow()

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
