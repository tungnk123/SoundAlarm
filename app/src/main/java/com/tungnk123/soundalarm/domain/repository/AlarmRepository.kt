package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun toggleAlarm(id: Long, isEnabled: Boolean)
    suspend fun getEnabledAlarms(): List<Alarm>
}
