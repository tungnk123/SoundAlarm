package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.MusicTrack
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<MusicTrack>>
    fun getTracksByPlaylistGroup(playlistGroupId: Long): Flow<List<MusicTrack>>
    fun getFavoriteTracks(): Flow<List<MusicTrack>>
    suspend fun getPlaylistTracks(): List<MusicTrack>
    suspend fun getTracksByPlaylistGroupList(playlistGroupId: Long): List<MusicTrack>
    suspend fun addTrack(track: MusicTrack, playlistGroupId: Long = 0)
    suspend fun removeTrack(track: MusicTrack)
    suspend fun setFavorite(trackId: Long, isFavorite: Boolean)
    suspend fun updateTrackOrder(tracks: List<MusicTrack>)
}
