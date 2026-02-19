package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.MusicTrack
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<MusicTrack>>
    suspend fun getPlaylistTracks(): List<MusicTrack>
    suspend fun addTrack(track: MusicTrack)
    suspend fun removeTrack(track: MusicTrack)
    suspend fun updateTrackOrder(tracks: List<MusicTrack>)
}
