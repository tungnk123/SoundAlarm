package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.PlaylistDao
import com.tungnk123.soundalarm.data.local.entity.PlaylistEntity
import com.tungnk123.soundalarm.domain.model.MusicTrack
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getPlaylist(): Flow<List<MusicTrack>> =
        playlistDao.getAllTracks().map { entities -> entities.map { it.toDomain() } }

    override fun getTracksByPlaylistGroup(playlistGroupId: Long): Flow<List<MusicTrack>> =
        playlistDao.getTracksByPlaylistGroup(playlistGroupId).map { entities -> entities.map { it.toDomain() } }

    override fun getFavoriteTracks(): Flow<List<MusicTrack>> =
        playlistDao.getFavoriteTracks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getPlaylistTracks(): List<MusicTrack> =
        playlistDao.getAllTracksList().map { it.toDomain() }

    override suspend fun getTracksByPlaylistGroupList(playlistGroupId: Long): List<MusicTrack> =
        playlistDao.getTracksByPlaylistGroupList(playlistGroupId).map { it.toDomain() }

    override suspend fun addTrack(track: MusicTrack, playlistGroupId: Long) {
        val currentCount = playlistDao.getAllTracksList().size
        playlistDao.insertTrack(
            PlaylistEntity.fromDomain(track, sortOrder = currentCount, playlistGroupId = playlistGroupId)
        )
    }

    override suspend fun removeTrack(track: MusicTrack) {
        val entity = PlaylistEntity.fromDomain(track).copy(id = track.id)
        playlistDao.deleteTrack(entity)
    }

    override suspend fun setFavorite(trackId: Long, isFavorite: Boolean) {
        playlistDao.setFavorite(trackId, isFavorite)
    }

    override suspend fun updateTrackOrder(tracks: List<MusicTrack>) {
        val entities = tracks.mapIndexed { index, track ->
            PlaylistEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                duration = track.duration,
                contentUri = track.contentUri.toString(),
                sortOrder = index,
                playlistGroupId = track.playlistGroupId,
                isFavorite = track.isFavorite,
            )
        }
        playlistDao.updateTracks(entities)
    }
}
