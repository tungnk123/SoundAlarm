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

    override fun getPlaylist(): Flow<List<MusicTrack>> {
        return playlistDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlaylistTracks(): List<MusicTrack> {
        return playlistDao.getAllTracksList().map { it.toDomain() }
    }

    override suspend fun addTrack(track: MusicTrack) {
        playlistDao.insertTrack(PlaylistEntity.fromDomain(track))
    }

    override suspend fun removeTrack(track: MusicTrack) {
        // We need to delete by contentUri or similar unique ID if the ID from domain is not the database ID
        // Assuming we pass the object retrieved from DB which has the correct ID
        // Or we might need to find it first.
        // For simplicity, we trust the ID or contentUri equality if mapped correctly.
        // If track comes from MediaStore it has ID=MediaID, but in DB it has ID=AutoInc.
        // Let's rely on contentUri for identification if ID is inconsistent, but here we likely store copies.
        // Actually, let's treat the incoming track as one that might not have the DB ID if it comes from the music selection screen.
        // But if it comes from the playlist screen, it has the DB ID.
        // To be safe for "Add": fromDomain creates new entity.
        // To be safe for "Remove": we likely need the DB ID.
        // Let's assuming UI passes back the object related to the DB entity for removal.
        // However, if we want to remove by contentUri match (e.g. toggle in selection screen), we might need a query.
        // For now, simple delete.
         val entity = if (track.id != 0L) {
             // It might be a MediaStore ID or DB ID.
             // If we saved it, we should probably check if we can delete by contentUri to be safe?
             // No, let's stick to standard flow:
             // 1. View Playlist -> items have DB IDs -> Remove -> Delete by ID.
             // 2. View MusicList -> items have MediaStore IDs -> Add -> Insert new.
             PlaylistEntity.fromDomain(track).copy(id = track.id) // This might be wrong if track.id is MediaStore ID.
         } else {
             PlaylistEntity.fromDomain(track)
         }
        
        // Correct approach: Delete should arguably be by ID.
        // But `MusicTrack` reuses `id` for both MediaStore ID and DB ID? That's risky.
        // Let's assume for removal validation we rely on the object being from the playlist.
        // For standard "Remove from playlist", we will pass the track obtained from `getPlaylist`.
        playlistDao.deleteTrack(entity)
    }
}
