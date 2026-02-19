package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.AlarmDayTrackDao
import com.tungnk123.soundalarm.data.local.entity.AlarmDayTrackEntity
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmDayTrackRepositoryImpl @Inject constructor(
    private val dao: AlarmDayTrackDao,
) : AlarmDayTrackRepository {

    override fun getTracksForAlarm(alarmId: Long): Flow<List<AlarmDayTrack>> =
        dao.getTracksForAlarmFlow(alarmId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTracksForAlarmSync(alarmId: Long): List<AlarmDayTrack> =
        dao.getTracksForAlarm(alarmId).map { it.toDomain() }

    override suspend fun setTrackForDay(
        alarmId: Long,
        dayOfWeek: String,
        trackUri: String,
        trackTitle: String,
        trackArtist: String,
    ) {
        dao.upsertTrack(
            AlarmDayTrackEntity(
                alarmId = alarmId,
                dayOfWeek = dayOfWeek,
                trackUri = trackUri,
                trackTitle = trackTitle,
                trackArtist = trackArtist,
            )
        )
    }

    override suspend fun removeTrackForDay(alarmId: Long, dayOfWeek: String) {
        dao.deleteTrackForDay(alarmId, dayOfWeek)
    }

    override suspend fun deleteAllTracksForAlarm(alarmId: Long) {
        dao.deleteAllTracksForAlarm(alarmId)
    }
}

private fun AlarmDayTrackEntity.toDomain() = AlarmDayTrack(
    id = id,
    alarmId = alarmId,
    dayOfWeek = dayOfWeek,
    trackUri = trackUri,
    trackTitle = trackTitle,
    trackArtist = trackArtist,
)
