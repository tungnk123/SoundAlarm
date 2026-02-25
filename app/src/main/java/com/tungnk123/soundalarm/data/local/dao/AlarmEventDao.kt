package com.tungnk123.soundalarm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tungnk123.soundalarm.data.local.entity.AlarmEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmEventDao {

    @Insert
    suspend fun insertEvent(event: AlarmEventEntity)

    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<AlarmEventEntity>>

    @Query("DELETE FROM alarm_events")
    suspend fun clearAllEvents()
}
