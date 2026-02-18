package com.tungnk123.soundalarm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tungnk123.soundalarm.data.local.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllTracks(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlist")
    suspend fun getAllTracksList(): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: PlaylistEntity)

    @Delete
    suspend fun deleteTrack(track: PlaylistEntity)
}
