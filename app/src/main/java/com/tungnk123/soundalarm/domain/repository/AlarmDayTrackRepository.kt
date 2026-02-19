package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import kotlinx.coroutines.flow.Flow

interface AlarmDayTrackRepository {
    fun getTracksForAlarm(alarmId: Long): Flow<List<AlarmDayTrack>>
    suspend fun getTracksForAlarmSync(alarmId: Long): List<AlarmDayTrack>
    suspend fun setTrackForDay(
        alarmId: Long,
        dayOfWeek: String,
        trackUri: String,
        trackTitle: String,
        trackArtist: String,
    )
    suspend fun removeTrackForDay(alarmId: Long, dayOfWeek: String)
    suspend fun deleteAllTracksForAlarm(alarmId: Long)
}
