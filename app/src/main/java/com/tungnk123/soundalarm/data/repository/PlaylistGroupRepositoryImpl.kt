package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.PlaylistGroupDao
import com.tungnk123.soundalarm.data.local.entity.PlaylistGroupEntity
import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import com.tungnk123.soundalarm.domain.repository.PlaylistGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistGroupRepositoryImpl @Inject constructor(
    private val playlistGroupDao: PlaylistGroupDao,
) : PlaylistGroupRepository {

    override fun getAllPlaylistGroups(): Flow<List<PlaylistGroup>> =
        playlistGroupDao.getAllPlaylistGroups().map { entities ->
            entities.map { entity ->
                val count = playlistGroupDao.getTrackCount(entity.id)
                entity.toDomain(count)
            }
        }

    override suspend fun createPlaylistGroup(name: String): Long =
        playlistGroupDao.insertPlaylistGroup(PlaylistGroupEntity(name = name))

    override suspend fun renamePlaylistGroup(id: Long, name: String) {
        val entity = playlistGroupDao.getPlaylistGroupById(id) ?: return
        playlistGroupDao.updatePlaylistGroup(entity.copy(name = name))
    }

    override suspend fun deletePlaylistGroup(id: Long) {
        val entity = playlistGroupDao.getPlaylistGroupById(id) ?: return
        playlistGroupDao.deletePlaylistGroup(entity)
    }
}
