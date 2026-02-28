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

    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC LIMIT 20")
    fun getRecentEvents(): Flow<List<AlarmEventEntity>>

    @Query("SELECT COUNT(*) FROM alarm_events WHERE eventType = :type")
    fun countByEventType(type: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM alarm_events WHERE eventType = 'DISMISSED' AND dismissMethod = :method")
    fun countDismissedByMethod(method: String): Flow<Int>

    @Query("DELETE FROM alarm_events")
    suspend fun clearAllEvents()
}
