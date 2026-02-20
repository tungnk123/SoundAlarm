package com.tungnk123.soundalarm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tungnk123.soundalarm.data.local.entity.PlaylistGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistGroupDao {
    @Query("SELECT * FROM playlist_groups ORDER BY createdAt ASC")
    fun getAllPlaylistGroups(): Flow<List<PlaylistGroupEntity>>

    @Query("SELECT * FROM playlist_groups WHERE id = :id")
    suspend fun getPlaylistGroupById(id: Long): PlaylistGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistGroup(group: PlaylistGroupEntity): Long

    @Update
    suspend fun updatePlaylistGroup(group: PlaylistGroupEntity)

    @Delete
    suspend fun deletePlaylistGroup(group: PlaylistGroupEntity)

    @Query("SELECT COUNT(*) FROM playlist WHERE playlistGroupId = :groupId")
    suspend fun getTrackCount(groupId: Long): Int
}
