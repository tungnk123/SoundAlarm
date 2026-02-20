package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.PlaylistGroup
import kotlinx.coroutines.flow.Flow

interface PlaylistGroupRepository {
    fun getAllPlaylistGroups(): Flow<List<PlaylistGroup>>
    suspend fun createPlaylistGroup(name: String): Long
    suspend fun renamePlaylistGroup(id: Long, name: String)
    suspend fun deletePlaylistGroup(id: Long)
}
