package com.tungnk123.soundalarm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tungnk123.soundalarm.data.local.entity.AlarmDayTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDayTrackDao {

    @Query("SELECT * FROM alarm_day_tracks WHERE alarmId = :alarmId")
    fun getTracksForAlarmFlow(alarmId: Long): Flow<List<AlarmDayTrackEntity>>

    @Query("SELECT * FROM alarm_day_tracks WHERE alarmId = :alarmId")
    suspend fun getTracksForAlarm(alarmId: Long): List<AlarmDayTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: AlarmDayTrackEntity)

    @Query("DELETE FROM alarm_day_tracks WHERE alarmId = :alarmId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteTrackForDay(alarmId: Long, dayOfWeek: String)

    @Query("DELETE FROM alarm_day_tracks WHERE alarmId = :alarmId")
    suspend fun deleteAllTracksForAlarm(alarmId: Long)
}
