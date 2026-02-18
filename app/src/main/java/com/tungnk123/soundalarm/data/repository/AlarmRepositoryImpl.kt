package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.AlarmDao
import com.tungnk123.soundalarm.data.local.entity.AlarmEntity
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(AlarmEntity.fromDomain(alarm))
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmEntity.fromDomain(alarm))
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(alarm))
    }

    override suspend fun toggleAlarm(id: Long, isEnabled: Boolean) {
        alarmDao.toggleAlarm(id, isEnabled)
    }

    override suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomain() }
    }
}
