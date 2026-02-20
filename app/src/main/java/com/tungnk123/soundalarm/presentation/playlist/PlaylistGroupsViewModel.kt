package com.tungnk123.soundalarm.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import com.tungnk123.soundalarm.domain.repository.PlaylistGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistGroupsEvent {
    data class GroupDeleted(val name: String) : PlaylistGroupsEvent
}

@HiltViewModel
class PlaylistGroupsViewModel @Inject constructor(
    private val repository: PlaylistGroupRepository,
) : ViewModel() {

    val playlistGroups: StateFlow<List<PlaylistGroup>> = repository.getAllPlaylistGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _events = MutableSharedFlow<PlaylistGroupsEvent>()
    val events: SharedFlow<PlaylistGroupsEvent> = _events.asSharedFlow()

    fun createPlaylistGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylistGroup(name.trim())
        }
    }

    fun renamePlaylistGroup(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylistGroup(id, newName.trim())
        }
    }

    fun deletePlaylistGroup(group: PlaylistGroup) {
        viewModelScope.launch {
            repository.deletePlaylistGroup(group.id)
            _events.emit(PlaylistGroupsEvent.GroupDeleted(group.name))
        }
    }
}
